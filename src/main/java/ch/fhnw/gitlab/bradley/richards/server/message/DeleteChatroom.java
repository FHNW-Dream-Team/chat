package ch.fhnw.gitlab.bradley.richards.server.message;

import chatroom.server.Chatroom;
import chatroom.server.Client;

public class DeleteChatroom extends Message {
	private String token;
	private String name;
	
	public DeleteChatroom(String[] data) {
		super(data);
		this.token = data[1];
		this.name = data[2];
	}

	/**
	 * Only the owner of a chatroom can delete it
	 */
	@Override
	public void process(Client client) {
		boolean result = false;
		if (client.getToken().equals(token)) {
			Chatroom chatroom = Chatroom.exists(name);
			if (chatroom != null && chatroom.getOwner().equals(client.getName())) {
				Chatroom.remove(chatroom);
				result = true;
			}
		}
		client.send(new Result(this.getClass(), result));
	}


}
