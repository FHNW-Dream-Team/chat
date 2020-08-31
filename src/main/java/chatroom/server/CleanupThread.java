package chatroom.server;

import java.util.logging.Logger;

public class CleanupThread extends Thread {
	private static Logger logger = Logger.getLogger("");

	public CleanupThread() {
		super();
		this.setName("CleanupThread");
	}

	@Override
	public void run() {
		while (true) {
			logger.info("Cleanup process triggered");

			// Clean up clients
			Client.cleanupClients();

			// Clean up accounts
			Account.cleanupAccounts();
			Account.saveAccounts();

			// Clean up chatrooms
			Chatroom.cleanupChatrooms();
			Chatroom.saveChatrooms();

			System.gc();

			// Log status
			long freeMemory = Runtime.getRuntime().freeMemory();
			freeMemory /= (1024 * 1024);
			logger.info("Cleanup process complete; " + freeMemory + "MB available, " +
						currentThread().activeCount() + " threads running");

			try {
				Thread.sleep(300000); // Every 5 minutes
			} catch (InterruptedException e) {
			}
		}
	}
}
