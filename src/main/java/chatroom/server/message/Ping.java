package chatroom.server.message;

import chatroom.server.Client;

public class Ping extends Message {
	private String token;

	/**
	 * The data may optionally contain a token
	 */
	public Ping(String[] data) {
		super(data);
		token = null;
		if (data.length > 1) token = data[1];
	}

	/**
	 * - If no token is present, we answer with success
	 * - If a token is present, we answer with success if the token matches this client
	 */
	@Override
	public void process(Client client) {
		boolean result = (token == null || token.equals(client.getToken()));
		client.send(new Result(this.getClass(), result));
	}

}
