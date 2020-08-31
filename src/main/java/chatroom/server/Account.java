package chatroom.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * This class represents a registered client, i.e., one that has defined a
 * username and password
 * 
 * At the class level, we maintain a list of all registered clients.
 * 
 * Passwords are hashed securely, using one of the algorithms built into Java.
 * If this algorithm somehow not exist, this is catastrophic, and we stop the
 * server.
 */
public class Account implements Serializable {
	private static final long serialVersionUID = 1;
	private static Logger logger = Logger.getLogger("");

	private static final ArrayList<Account> accounts = new ArrayList<>();
	private static final SecureRandom rand = new SecureRandom();
	private static final int iterations = 127;

	private final String username;
	private final byte[] salt = new byte[64];
	private String hashedPassword;
	private Instant lastLogin;

	/**
	 * Add a new account to our list of valid accounts
	 */
	public static void add(Account account) {
		synchronized (accounts) {
			accounts.add(account);
		}
	}

	/**
	 * Remove a account from our list of valid accounts
	 */
	public static void remove(Account account) {
		synchronized (accounts) {
			for (Iterator<Account> i = accounts.iterator(); i.hasNext();) {
				if (account == i.next()) i.remove();
			}
		}
	}

	/**
	 * Find and return an existing account
	 */
	public static Account exists(String username) {
		synchronized (accounts) {
			for (Account account : accounts) {
				if (account.username.equals(username)) return account;
			}
		}
		return null;
	}

	/**
	 * Clean up old accounts -- called by cleanup thread
	 */
	public static void cleanupAccounts() {
		synchronized (accounts) {
			Instant expiry = Instant.now().minusSeconds(3 * 86400); // 3 days
			logger.fine("Cleanup accounts: " + accounts.size() + " accounts registered");
			for (Iterator<Account> i = accounts.iterator(); i.hasNext();) {
				Account account = i.next();
				if (account.lastLogin.isBefore(expiry)) {
					logger.fine("Cleanup accounts: removing account " + account.getUsername());
					i.remove();
				}
			}
			logger.fine("Cleanup accounts: " + accounts.size() + " accounts registered");
		}
	}

	/**
	 * Save accounts to disk -- called by cleanup thread
	 */
	public static void saveAccounts() {
		File accountFile = new File(Server.getHome() + "accounts.sav");
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(accountFile))) {
			synchronized (accounts) {
				out.writeInt(accounts.size());
				for (Account account : accounts) {
					out.writeObject(account);
				}
				out.flush();
				out.close();
			}
		} catch (IOException e) {
			logger.severe("Unable to save accounts: " + e.getMessage());
		}
	}

	/**
	 * Read accounts at program start. No synchronization needed, since no threads
	 * are running
	 */
	public static void readAccounts() {
		File accountFile = new File(Server.getHome() + "accounts.sav");
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(accountFile))) {
			int num = in.readInt();
			for (int i = 0; i < num; i++) {
				Account account = (Account) in.readObject();
				accounts.add(account);
				logger.fine("Loaded account " + account.getUsername());
			}
		} catch (Exception e) {
			logger.severe("Unable to read accounts: " + e.getMessage());
		}
	}

	/**
	 * This method is here, because we have a secure random number generator already
	 * set up. We have a 32-character token - enough to be reasonably secure.
	 */
	public static String getToken() {
		byte[] token = new byte[16];
		rand.nextBytes(token);
		return bytesToHex(token);
	}

	public Account(String username, String password) {
		this.username = username;
		rand.nextBytes(salt);
		this.hashedPassword = hash(password);
		this.lastLogin = Instant.now();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || o.getClass() != this.getClass()) return false;
		Account ol = (Account) o;
		return ol.username.equals(this.username);
	}

	public boolean checkPassword(String password) {
		String newHash = hash(password);
		boolean success = hashedPassword.equals(newHash);
		if (success) this.lastLogin = Instant.now();
		return success;
	}

	public void changePassword(String newPassword) {
		rand.nextBytes(salt); // Change the salt with the password!
		this.hashedPassword = hash(newPassword);
	}

	public String getUsername() {
		return username;
	}

	/**
	 * There are many sources of info on how to securely hash passwords. I'm not a
	 * crypto expert, so I follow the recommendations of the experts. Here are two
	 * examples:
	 * 
	 * https://crackstation.net/hashing-security.htm
	 * 
	 * https://howtodoinjava.com/security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
	 */
	private String hash(String password) {
		try {
			char[] chars = password.toCharArray();
			PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hash = skf.generateSecret(spec).getEncoded();
			return bytesToHex(hash);
		} catch (Exception e) {
			logger.severe("Secure password hashing not possible - stopping server");
			System.exit(0);
			return null; // Will never execute, but keeps Java happy
		}
	}

	// From:
	// https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
