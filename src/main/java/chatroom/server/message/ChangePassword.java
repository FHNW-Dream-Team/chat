package chatroom.server.message;

import chatroom.server.Account;
import chatroom.server.Client;

public class ChangePassword extends Message {
	private String token;
	private String password;

	public ChangePassword(String[] data) {
		super(data);
		this.token = data[1];
		this.password = data[2];
	}

	@Override
	public void process(Client client) {
		boolean result = false;
		if (client.getToken().equals(token)) {
			Account account = client.getAccount();
			account.changePassword(password);
			result = true;
		}
		client.send(new Result(this.getClass(), result));
	}

}
