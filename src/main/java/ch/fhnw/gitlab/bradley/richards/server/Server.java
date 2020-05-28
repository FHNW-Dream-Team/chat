package chatroom.server;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server {
	private static final Logger logger = Logger.getLogger("");
	private static int port = -1;
	private static String homeDirectory = "";
	private static boolean secure = false;
	
	public static void main(String[] args) {
		// Setup logging, including a file handler
		setupLogging();
		
		// Reload any existing chatrooms and accounts
		logger.info("Read any existing data");
		Chatroom.readChatrooms();
		Account.readAccounts();
		
		try {
			// Read command-line parameters, if they exist - otherwise read from the console
			if (args.length > 0) {
				logger.info("Process command-line parameters");
				port = Integer.parseInt(args[0]);
				if (args.length > 1) homeDirectory = args[1];
				if (args.length > 2) secure = args[2].equalsIgnoreCase("yes");
			} else {
				readOptions();
			}
			
			// Start the listener
			ListenerThread lt = new ListenerThread(port, secure);
			lt.start();
			
			// Start the clean-up thread
			CleanupThread ct = new CleanupThread();
			ct.start();
		} catch (IOException e) {
			if (secure && e.getCause() instanceof GeneralSecurityException) {
				logger.severe("Error creating secure socket connection - does the keystore exist?");
			}
			logger.info(e.toString());
		}
	}
	
	private static void setupLogging() {
		logger.setLevel(Level.FINE);
		logger.getHandlers()[0].setLevel(Level.WARNING); // Standard (console) handler
		try {
			FileHandler fh = new FileHandler("%h/ChatroomServer_%u_%g.log", 10000000, 2);
			fh.setFormatter(new SimpleFormatter());
			fh.setLevel(Level.FINE);
			logger.addHandler(fh);
		} catch (Exception e) {
			logger.severe("Unable to create file handler for logging: " + e.toString());
			throw new RuntimeException("Unable to initialize log files: " + e.toString());
		}
	}

	private static void readOptions() {
		logger.info("Read start-up info from the console");
		try (Scanner in = new Scanner(System.in)) {
			while (port < 1024 || port > 65535) {
				System.out.println("Enter the port number (1024-65535): ");
				String s = in.nextLine();
				try {
					port = Integer.parseInt(s);
				} catch (NumberFormatException e) {
					// do nothing
				}
			}
			
			System.out.println("Enter the home directory (leave empty for current location)");
			String s = in.nextLine().trim();
			if (s.length() > 0 && s.charAt(s.length()-1) != '/') s += "/";
			homeDirectory = s;
			
			System.out.println("Enter 'yes' if the server should use SecureSockets");
			s = in.nextLine().trim();
			secure = s.equalsIgnoreCase("yes");
		}
	}
	
	public static String getHome() { return homeDirectory; }
}
