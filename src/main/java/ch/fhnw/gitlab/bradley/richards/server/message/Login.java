package ch.fhnw.gitlab.bradley.richards.server.message;

import chatroom.server.Account;
import chatroom.server.Client;

/**
 * Login to an existing account. If successful, return an authentication token
 * to the client.
 */
public class Login extends Message {
	private String username;
	private String password;

	public Login(String[] data) {
		super(data);
		this.username = data[1];
		this.password = data[2];
	}

	@Override
	public void process(Client client) {
		Message reply;
		// Find existing login matching the username
		Account account = Account.exists(username);
		if (account != null && account.checkPassword(password)) {
			client.setAccount(account);
			String token = Account.getToken();
			client.setToken(token);
			reply = new Result(this.getClass(), true, token);
		} else {
			reply = new Result(this.getClass(), false);
		}
		client.send(reply);
	}
}
