package chatroom.server.message;

import chatroom.server.Account;
import chatroom.server.Chatroom;
import chatroom.server.Client;

public class CreateChatroom extends Message {
	private String token;
	private String name;
	private boolean isPublic;

	public CreateChatroom(String[] data) {
		super(data);
		this.token = data[1];
		this.name = data[2];
		this.isPublic = data[3].equalsIgnoreCase("true");
	}

	/**
	 * We can only create a new chatroom if the name is not in use: not as a user and not as a chatroom.
	 * Valid names are at least 3 characters long
	 * @param client
	 */
	@Override
	public void process(Client client) {
		boolean result = false;
		if (client.getToken().equals(token)) {
			if (name.length() >= 3 && Account.exists(name) == null && Chatroom.exists(name) == null) {
				Chatroom chatroom = new Chatroom(name, isPublic, client.getName());
				Chatroom.add(chatroom);
				result = true;
			}
		}
		client.send(new Result(this.getClass(), result));
	}
}
