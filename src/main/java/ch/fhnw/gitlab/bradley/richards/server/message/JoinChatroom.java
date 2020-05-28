package ch.fhnw.gitlab.bradley.richards.server.message;

import chatroom.server.Chatroom;
import chatroom.server.Client;

/**
 * Add a user as a member of a chatroom.
 */
public class JoinChatroom extends Message {
	private String token;
	private String name;
	private String username;

	public JoinChatroom(String[] data) {
		super(data);
		this.token = data[1];
		this.name = data[2];
		this.username = data[3];
	}

	/**
	 * The owner of a chatroom can add anyone. A user can add themselves to any
	 * public chatroom. Otherwise, no one can add anyone.
	 */
	@Override
	public void process(Client client) {
		boolean result = false;
		if (client.getToken().equals(token)) {
			Chatroom chatroom = Chatroom.exists(name);
			if (chatroom.getOwner().equals(client.getName())
					|| client.getName().equals(username) && chatroom.isPublic()) {
				chatroom.addUser(username);
				result = true;
			}
		}
		client.send(new Result(this.getClass(), result));
	}
}
