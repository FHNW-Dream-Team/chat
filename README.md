# fhnw-chat

Java project for running the client part of the chat program

Project

[![License](https://img.shields.io/github/license/d3strukt0r/fhnw-chat)][license]

master-branch (alias stable, latest)

[![GH Action CI/CD](https://github.com/D3strukt0r/fhnw-chat/workflows/CI/CD/badge.svg?branch=master)][gh-action]
[![Codacy grade](https://img.shields.io/codacy/grade/15382f33edbe4e209d394a4e8a3b9801/master)][codacy]

develop-branch (alias nightly)

[![GH Action CI/CD](https://github.com/D3strukt0r/fhnw-chat/workflows/CI/CD/badge.svg?branch=develop)][gh-action]
[![Codacy grade](https://img.shields.io/codacy/grade/15382f33edbe4e209d394a4e8a3b9801/develop)][codacy]

## Getting Started

Use a console version or GUI version. When running there are additional options (check the usage part).

### Basic features: Functionality

-   ✔ Set server address and port
-   ✔ Create new login
-   ✔ Change password
-   ✔ Delete account
-   ✔ Login
-   ✔ Logout
-   ✔ List group chats
-   ✔ Join a particular group chats
-   ✔ See messages in the group chats
-   ✔ Send a message to the group chats

### Basic features: Architecture and GUI

-   ✔ MVC structure: Separate the UI from the logic
-   ✔ Multilingual application: English, German, Italian, French
-   ✔ Reasonable user interface
-   ✔ Platform independent
-   ✔ User errors do not crash the program
-   ✔ Controls enable/disable as appropriate

### Optional: Contacts and private messaging

#### Simple: Maintain a list of contacts

-   ❌ Display status of contact
-   ✔ Be able to send/receive private messages to a contact
-   ✔ How will you distinguish private messages from public chat? Different tabs!

#### Advanced: Maintain a block list

-   ❌ Users whose private messages will be ignored
-   ❌ Users whose chat room messages will not be displayed
-   ❌ Are these the same users, or two different lists?

### Optional: GUI features

#### Simple: Implement a nice GUI

-   ✔ Nice design, good use of color
-   ✔ Good error messages
-   ✔ Sensible resizing behavior
-   ✔ All controls enabled/disabled when it makes sense

#### Advanced: “Wow” factor

-   ✔ Subjective…
-   ❌ For example: animations for user feedback

### Optional: Chat room usability features

#### Simple

-   ❌ Logout / Login – pick up where you left off (Annoying feature)
-   ❌ Display list of users in the group chats

#### Advanced

-   ✔ Be able to join multiple chat rooms
-   ❌ Manage a private chat room (Add and remove users)

### Optional: Further features

-   ✔ Maintain long-term history of chats
-   ❌ Be able to select portion of chat to look at
-   ❌ Offer search/filter functionality – by user, by content, by date

## Installation

Import to your IDE and run

## Usage

When running you can add following options:

| Short | Long          | Description                                                                   |
| ----- | ------------- | ----------------------------------------------------------------------------- |
| -g    | --no-gui      | Uses the console as interface instead of a window                             |
| -d    | --no-db       | Don't use the internal db. Data will never be saved.                          |
| -l    | --db-location | If using a DB (by default on), use a custom location for the file             |
| -v    | --verbose     | Show more information in the console. Useful for debuggin and finding errors. |

## Documentation

-   Official site: [http://javaprojects.ch](http://javaprojects.ch)
-   Official ip: 147.86.8.31
-   Official port: 50001

### Client -> Server

| MessageType       | Data               | Notes                                                                                                            |
| ----------------- | ------------------ | ---------------------------------------------------------------------------------------------------------------- |
| CreateLogin       | Username, Password | Fails if name already taken (user or group chat), or invalid After creating an account, you still have to login  |
| Login             | Username, Password | Fails if name/password do not match                                                                              |
| ChangePassword    | New password       | Fails only if token is invalid                                                                                   |
| DeleteLogin       | -                  | Fails only if token is invalid; after delete, token becomes invalid                                              |
| Logout            | -                  | Never fails; token becomes invalid                                                                               |
| CreateChatroom    | Name, isPublic     | Fails if name already taken (user or group chat), or invalid After creating a group chat, you still have to join |
| JoinChatroom      | Chatroom, User     | User can add themselves to public group chat Only the creator can add user to a private group chat               |
| LeaveChatroom     | Chatroom, User     | You can always remove yourself. Group chat creator can remove anyone                                             |
| DeleteChatroom    | Chatroom           | Only the creator can delete a group chat                                                                         |
| ListChatrooms     | -                  | Returns a list of all public group chats                                                                         |
| Ping              | Token              | Without a token: always succeeds With token: succeeds only if token is valid                                     |
| SendMessage       | Target, Message    | Send message to user or group chat. Fails if user not online / Fails if not a member of the group chat           |
| UserOnline        | User               | Succeeds if the user is currently logged in                                                                      |
| ListChatroomUsers | Chatroom           | Returns a list of all users in the given group chat. You must be a member of this group chat                     |

### Client <- Server

#### Responses to client commands

| MessageType  | Data                               | Notes                                                                                                                                                              |
| ------------ | ---------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Result       | Boolean Boolean Token Boolean List | True if the command succeeded, otherwise false After a successful login, also returns the authentication token When a list is requested, also returns list results |
| MessageError | Error message                      | Incorrect commands, wrong number of arguments, etc.                                                                                                                |

#### Server initiated messages

| MessageType | Data               | Notes                                                                                                      |
| ----------- | ------------------ | ---------------------------------------------------------------------------------------------------------- |
| MessageText | Name, Target, Text | Name of user sending message Target is where the message was sent (group chat or user) Text of the message |

## Built With

-   [Maven](https://maven.apache.org/) - Dependency Management

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/D3strukt0r/fhnw-chat/tags).

## Authors

-   **Manuele Vaccari** - _Initial work_ - [D3strukt0r](https://github.com/D3strukt0r)

See also the list of [contributors](https://github.com/D3strukt0r/fhnw-chat/contributors) who participated in this project.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE.txt](LICENSE.txt) file for details.

## Acknowledgments

-   Hat tip to anyone whose code was used
-   Inspiration
-   etc

[license]: https://github.com/D3strukt0r/fhnw-chat/blob/master/LICENSE.txt
[gh-action]: https://github.com/D3strukt0r/fhnw-chat/actions
[codacy]: https://app.codacy.com/manual/D3strukt0r/fhnw-chat/dashboard
