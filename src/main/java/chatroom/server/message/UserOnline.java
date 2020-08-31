package chatroom.server.message;

import chatroom.server.Client;

public class UserOnline extends Message {
	private String token;
	private String username;

	public UserOnline(String[] data) {
		super(data);
		this.token = data[1];
		this.username = data[2];
	}

	/**
	 * Anyone can query a specific user: are they currently logged in?
	 *
	 * Note that "false" can also mean that the user asking the question is not
	 * logged in, and therefore cannot ask this question.
	 */
	@Override
	public void process(Client client) {
		boolean result = false;
		if (client.getToken() != null && client.getToken().equals(token)) {
			Client c = Client.exists(username);
			result = c != null && c.getToken() != null;
		}
		client.send(new Result(this.getClass(), result));
	}
}
