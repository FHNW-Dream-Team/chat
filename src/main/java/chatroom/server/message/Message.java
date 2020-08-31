package chatroom.server.message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

import chatroom.server.Client;

public abstract class Message {
	private static Logger logger = Logger.getLogger("");

	private String[] data;

	public Message(String[] data) {
		this.data = data;
	}

	// Special constructor for variable-length messages
	public Message(String[] data, ArrayList<String> elements) {
		this.data = new String[data.length + elements.size()];
		for (int i = 0; i < data.length; i++)
			this.data[i] = data[i];
		for (int i = 0; i < elements.size(); i++)
			this.data[i + data.length] = elements.get(i);
	}

	/**
	 * Perform whatever actions are required for this particular type of message.
	 */
	public abstract void process(Client client);

	public void send(Socket socket) throws IOException {
		OutputStreamWriter out;
		out = new OutputStreamWriter(socket.getOutputStream());
		logger.info("Sending message: " + this.toString());
		out.write(this.toString() + "\n");
		out.flush();
	}

	public static Message receive(Socket socket) {
		BufferedReader in;
		Message msg = null;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String msgText = in.readLine(); // Will wait here for complete line
			logger.info("Receiving message: " + msgText);

			// Break message into individual parts, and remove extra spaces
			String[] parts = msgText.split("\\|");
			for (int i = 0; i < parts.length; i++) {
				parts[i] = parts[i].trim();
			}

			// Create a message object of the correct class, using reflection
			//
			// This would be more understandable - but a *lot* longer - if we used
			// a series of "if" statements:
			//
			// if (parts[0].equals("Login") msg = new Login(parts);
			// else if (parts[0].equals("Logout") msg = new Logout(parts);
			// else if ...
			// else ...
			String messageClassName = Message.class.getPackage().getName() + "." + parts[0];
			try {
				Class<?> messageClass = Class.forName(messageClassName);
				Constructor<?> constructor = messageClass.getConstructor(String[].class);
				msg = (Message) constructor.newInstance(new Object[] { parts });
				logger.info("Received message of type " + parts[0]);
			} catch (Exception e) {
				logger.warning("Received invalid message of type " + parts[0]);
			}
		} catch (IOException e) {
			logger.warning(e.toString());
		}
		return msg;
	}

	/**
	 * A message is really just a bunch of strings separated by vertical bars
	 */
	@Override
	public String toString() {
		return String.join("|", data);
	}
}
