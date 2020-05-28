package chatroom.server;

import chatroom.server.message.Message;

/**
 * We can send messages to a class that implements this interface.
 */
public interface Sendable {	
	public abstract String getName(); // The name of the target being sent a message
	public abstract void send(Message msg); // The method to sent to this target
}
