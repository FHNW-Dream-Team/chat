package chatroom.server.message;

import chatroom.server.Client;

public class MessageError extends Message {

	public MessageError() {
		super(new String[] {"MessageError", "Invalid command"});
	}

	/**
	 * This message type does no processing at all
	 */
	@Override
	public void process(Client client) {
	}
}
