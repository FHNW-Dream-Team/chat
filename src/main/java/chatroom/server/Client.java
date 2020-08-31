package chatroom.server;

import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import chatroom.server.message.Message;
import chatroom.server.message.MessageError;

/**
 * This class represents a client, from the perspective of the server. We
 * communicate with the client using a socket. If the client is logged in, then
 * we have a token representing the client's authorization to use the server.
 * 
 * The first time any client connects, we create the list to store clients, and
 * we start a "cleanup" thread that discards clients that have not sent or
 * received any messages in a long time.
 */
public class Client implements Sendable {
	private static Logger logger = Logger.getLogger("");
	private static final ArrayList<Client> clients = new ArrayList<>();

	private Account account = null;
	private String token = null;
	private Socket socket;
	private boolean clientReachable = true;
	private Instant lastUsage;

	/**
	 * Add a new client to our list of active clients.
	 */
	public static void add(Client client) {
		synchronized (clients) {
			clients.add(client);
		}
	}

	/**
	 * Returns a client, found by username
	 */
	public static Client exists(String username) {
		synchronized (clients) {
			for (Client c : clients) {
				if (c.getAccount() != null && c.getName().equals(username)) return c;
			}
		}
		return null;
	}

	/**
	 * Clean up old clients -- called by cleanup thread
	 */
	public static void cleanupClients() {
		synchronized (clients) {
			Instant expiryLoggedOut = Instant.now().minusSeconds(300);
			Instant expiryLoggedIn = Instant.now().minusSeconds(3600);
			logger.fine("Cleanup clients: " + clients.size() + " clients registered");
			for (Iterator<Client> i = clients.iterator(); i.hasNext();) {
				Client client = i.next();
				if (client.token == null && client.lastUsage.isBefore(expiryLoggedOut)
						|| client.token != null && client.lastUsage.isBefore(expiryLoggedIn)) {
					logger.fine("Cleanup clients: removing client " + client.getName());
					// Close the socket, ignoring any errors, and remove the client
					try {
						if (client.socket != null) client.socket.close();
					} catch (IOException e) {
						// We don't care about any errors
					}
					i.remove();
				}
			}
			logger.fine("Cleanup clients: " + clients.size() + " clients registered");
		}
	}

	/**
	 * Create a new client object, communicating over the given socket. Immediately
	 * start a thread to receive messages from the client.
	 */
	public Client(Socket socket) {
		this.socket = socket;
		this.lastUsage = Instant.now();

		// Create thread to read incoming messages
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					while (clientReachable) {
						Message msg = Message.receive(socket);

						// Note the syntax "Client.this" - writing "this" would reference the Runnable
						// object
						if (msg != null)
							msg.process(Client.this);
						else { // Invalid message or broken socket
							Client.this.send(new MessageError());
						}

						lastUsage = Instant.now();
					}
				} catch (Exception e) {
					logger.info("Client " + Client.this.getName() + " disconnected");
				} finally {
					// When the client is no longer reachable, remove authentication and account
					token = null;
					account = null;
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
		logger.info("New client created: " + this.getName());
	}

	@Override // Sendable
	public String getName() {
		String name = null;
		if (account != null) name = account.getUsername();
		return name;
	}

	/**
	 * Send a message to this client. In case of an exception, log the client out.
	 */
	@Override // Sendable
	public void send(Message msg) {
		try {
			msg.send(socket);
			lastUsage = Instant.now();
		} catch (IOException e) {
			logger.warning("Client " + Client.this.getName() + " unreachable; logged out");
			this.token = null;
			clientReachable = false;
		}
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Socket getSocket() {
		return socket;
	}

	public Instant getLastUsage() {
		return lastUsage;
	}
}
