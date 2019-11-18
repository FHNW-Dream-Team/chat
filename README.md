# Chat

Java project for running the client part of the chat program

## Installation

Import to your IDE and run

## Usage

Use your brain

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

# Documentation

 * Official site: http://javaprojects.ch
 * Official ip: 147.86.8.31
 * Official port: 50001

## Client -> Server

| MessageType       | Data               | Notes                                                                                                         |
|-------------------|--------------------|---------------------------------------------------------------------------------------------------------------|
| CreateLogin       | Username, Password | Fails if name already taken (user or chatroom), or invalid After creating an account, you still have to login |
| Login             | Username, Password | Fails if name/password do not match                                                                           |
| ChangePassword    | New password       | Fails only if token is invalid                                                                                |
| DeleteLogin       | -                  | Fails only if token is invalid; after delete, token becomes invalid                                           |
| Logout            | -                  | Never fails; token becomes invalid                                                                            |
| CreateChatroom    | Name, isPublic     | Fails if name already taken (user or chatroom), or invalid After creating a chatroom, you still have to join  |
| JoinChatroom      | Chatroom, User     | User can add themselves to public chatrooms Only the creator can add user to a private chatroom               |
| LeaveChatroom     | Chatroom, User     | You can always remove yourself Chatroom creator can remove anyone                                             |
| DeleteChatroom    | Chatroom           | Only the creator can delete a chatroom                                                                        |
| ListChatrooms     | -                  | Returns a list of all public chatrooms                                                                        |
| Ping              | [Token]            | Without a token: always succeeds With token: succeeds only if token is valid                                  |
| SendMessage       | Target, Message    | Send message to user or chatroom Fails if user not online / Fails if not a member of the chatroom             |
| UserOnline        | User               | Succeeds if the user is currently logged in                                                                   |
| ListChatroomUsers | Chatroom           | Returns a list of all users in the given chatroom You must be a member of this chatroom                       |

## Client <- Server

### Responses to client commands

| MessageType  | Data                               | Notes                                                                                                                                                              |
|--------------|------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Result       | Boolean Boolean Token Boolean List | True if the command succeeded, otherwise false After a successful login, also returns the authentication token When a list is requested, also returns list results |
| MessageError | Error message                      | Incorrect commands, wrong number of arguments, etc.                                                                                                                |

### Server initiated messages

| MessageType | Data               | Notes                                                                                                    |
|-------------|--------------------|----------------------------------------------------------------------------------------------------------|
| MessageText | Name, Target, Text | Name of user sending message Target is where the message was sent (chatroom or user) Text of the message |

## Example console log

Enter a valid IP address

127.0.0.1

Enter a valid port number

31415

Enter commands to server or ctrl-D to quit

CreateLogin|brad|mypassword

Received: Result|true

Login|brad|mypassword

Received: Result|true|4FA4563A5C2FFD1E703B49190DC348BD

CreateChatroom|4FA4563A5C2FFD1E703B49190DC348BD|CatChat|true

Received: Result|true

JoinChatroom|4FA4563A5C2FFD1E703B49190DC348BD|CatChat|brad

Received: Result|true

SendMessage|4FA4563A5C2FFD1E703B49190DC348BD|CatChat|Hello, all cat people!

Received: MessageText|brad|CatChat|Hello, all cat people!

Received: Result|true

SomeInvalidCommand

Received: MessageError|Invalid command

SendMessage|Wrong|Parameters

Received: MessageError|Invalid command

Logout

Received: Result|true

## License
[MIT](LICENSE.txt)
