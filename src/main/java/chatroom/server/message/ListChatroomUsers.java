package chatroom.server.message;

import java.util.ArrayList;

import chatroom.server.Chatroom;
import chatroom.server.Client;

public class ListChatroomUsers extends Message {
	private String token;
	private String name;

	public ListChatroomUsers(String[] data) {
		super(data);
		this.token = data[1];
		this.name = data[2];
	}

	/**
	 * Only allowed if the chatorom is public, or the client is a member of the
	 * chatroom
	 */
	@Override
	public void process(Client client) {
		boolean result = false;
		ArrayList<String> names = null;
		if (client.getToken().equals(token)) {
			Chatroom chatroom = Chatroom.exists(name);
			if (chatroom != null) {
				names = chatroom.getUsers();
				if (chatroom.isPublic() || names.contains(client.getName())) {
					result = true;
				}
			}
		}

		if (result) {
			client.send(new Result(this.getClass(), true, names));
		} else {
			client.send(new Result(this.getClass(), false));
		}
	}
}
