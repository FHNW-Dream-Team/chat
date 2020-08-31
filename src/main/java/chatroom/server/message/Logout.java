package chatroom.server.message;

import chatroom.server.Client;

public class Logout extends Message {

	public Logout(String[] data) {
		super(data);
	}

	@Override
	public void process(Client client) {
		client.setToken(null); // Destroy authentication token
		client.setAccount(null); // Destroy account information
		client.send(new Result(this.getClass(), true));
	}
}
