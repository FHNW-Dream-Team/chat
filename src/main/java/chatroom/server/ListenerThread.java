package chatroom.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Security;
import java.util.logging.Logger;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import com.sun.net.ssl.internal.ssl.Provider;

public class ListenerThread extends Thread {
	private static Logger logger = Logger.getLogger("");
	private final ServerSocket listener;
	private final int port;

	public ListenerThread(int port, boolean secure) throws IOException {
		super();
		this.port = port;
		this.setName("ListenerThread");

		if (secure) {
			// Registering the JSSE provider
			Security.addProvider(new Provider());

			// Specifying the Keystore details. Note that you must manually add or generate
			// keys using the Java keystore tool
			System.setProperty("javax.net.ssl.keyStore", "server.ks");
			System.setProperty("javax.net.ssl.keyStorePassword", "sseucpreert");

			// Initialize the Server Socket
			SSLServerSocketFactory sslServerSocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
					.getDefault();
			listener = (SSLServerSocket) sslServerSocketfactory.createServerSocket(port, 10, null);
		} else {
			listener = new ServerSocket(port, 10, null);
		}
	}

	@Override
	public void run() {
		logger.info("Starting listener on port " + port);
		while (true) {
			try {
				Socket socket = listener.accept();
				Client.add(new Client(socket));
			} catch (Exception e) {
				logger.info(e.toString());
			}
		}
	}
}
