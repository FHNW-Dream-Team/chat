package chatroom.server.message;

import chatroom.server.Chatroom;
import chatroom.server.Client;

/**
 * Remove a user as a member of a chatroom
 */
public class LeaveChatroom extends Message {
	private String token;
	private String name;
	private String username;

	public LeaveChatroom(String[] data) {
		super(data);
		this.token = data[1];
		this.name = data[2];
		this.username = data[3];
	}

	/**
	 * The owner of a chatroom can remove anyone. A user can remove themselves.
	 * Otherwise, no one can remove anyone.
	 */
	@Override
	public void process(Client client) {
		boolean result = false;
		if (client.getToken().equals(token)) {
			Chatroom chatroom = Chatroom.exists(name);
			if (chatroom.getOwner().equals(client.getName())
					|| client.getName().equals(username)) {
				chatroom.removeUser(username);
				result = true;
			}
		}
		client.send(new Result(this.getClass(), result));
	}
}
