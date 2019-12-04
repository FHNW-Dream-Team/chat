package com.orbitrondev.Controller;

import com.orbitrondev.EventListener.MessageErrorEventListener;
import com.orbitrondev.EventListener.MessageTextEventListener;
import com.orbitrondev.Exception.InvalidIpException;
import com.orbitrondev.Exception.InvalidPortException;
import com.orbitrondev.Main;
import com.orbitrondev.Model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * A class to handle the full console application. The connection, input and output.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 0.0.1
 */
public class CliController implements MessageTextEventListener, MessageErrorEventListener {
    private ServiceLocator sl;
    private static final Logger logger = LogManager.getLogger(CliController.class);

    private MainModel model;

    private BackendController backend;

    private Scanner input;

    /**
     * If desired, connect to the local database and use it's information. If nowhere saved, ask for IP address, port
     * and whether to use SSL.
     *
     * @param model An object containing the model to access all data and DB.
     *
     * @since 0.0.1
     */
    public CliController(MainModel model) {
        this.model = model;
        sl = ServiceLocator.getServiceLocator();
        sl.setModel(model);

        String ipAddress = null;
        int portNumber = -1;
        boolean secure = false;
        boolean askForSecure = true;
        boolean addToDB = true;

        input = new Scanner(System.in);

        if (Main.connectToDb) {
            sl.setDb(new DatabaseController(Main.dbLocation));

            int serverCounter = 0;
            ArrayList<ServerModel> serverList = new ArrayList<>();

            // Print all saved connections inside the database
            System.out.println(I18nController.get("console.setup.server.select"));
            System.out.println(I18nController.get("console.setup.server.create"));
            for (ServerModel server : sl.getDb().getServerDao()) {
                serverCounter++;
                serverList.add(server);
                if (server.isSecure()) {
                    System.out.println(I18nController.get("console.setup.server.entry.ssl", serverCounter, server.getIp(), Integer.toString(server.getPort())));
                } else {
                    System.out.println(I18nController.get("console.setup.server.entry", serverCounter, server.getIp(), Integer.toString(server.getPort())));
                }
            }

            int chosenServer = -1;
            while (chosenServer < 0) {
                System.out.print("$ ");
                if (input.hasNextLine()) {
                    String s = input.nextLine();

                    // Check whether the entered string is empty
                    if (s.length() == 0) {
                        System.out.println(I18nController.get("console.setup.server.entry.empty"));
                        continue;
                    }
                    int tempChosenServer;
                    try {
                        tempChosenServer = Integer.parseInt(s);

                        // Check whether we entered a known number (which refers to something)
                        if (tempChosenServer < 0 || tempChosenServer > serverCounter) {
                            System.out.println(I18nController.get("console.setup.server.entry.outOfRange", serverCounter));
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        // Check whether we entered a number
                        System.out.println(I18nController.get("console.setup.server.entry.nan"));
                        continue;
                    }
                    chosenServer = tempChosenServer;
                } else {
                    System.exit(0);
                }
            }

            // Save the known address if we entered something higher than 0
            chosenServer--; // The array starts at 0, but the output starts at 1
            if (chosenServer >= 0) {
                ipAddress = serverList.get(chosenServer).getIp();
                portNumber = serverList.get(chosenServer).getPort();
                secure = serverList.get(chosenServer).isSecure();
                askForSecure = false;
                addToDB = false;
            }
        }

        // If needed, ask for an IP address from the user
        if (ipAddress == null) {
            System.out.println(I18nController.get("console.setup.ip"));

            // Read IP address
            while (ipAddress == null) {
                System.out.print("$ ");
                if (input.hasNextLine()) {
                    String s = input.nextLine();

                    // Check whether the entered string is empty
                    if (s.length() == 0) {
                        System.out.println(I18nController.get("console.setup.ip.empty"));
                        continue;
                    }

                    // Check whether the entered string is a valid IP address
                    if (!BackendController.isValidIpAddress(s)) {
                        System.out.println(I18nController.get("console.setup.ip.notIp"));
                        continue;
                    }

                    ipAddress = s;
                } else {
                    System.exit(0);
                }
            }
        }

        // If needed, ask for an port from the user
        if (portNumber == -1) {
            System.out.println(I18nController.get("console.setup.port"));

            // Read port
            while (portNumber == -1) {
                System.out.print("$ ");
                if (input.hasNextLine()) {
                    String s = input.nextLine();

                    // Check whether the entered string is empty
                    if (s.length() == 0) {
                        System.out.println(I18nController.get("console.setup.port.empty"));
                        continue;
                    }

                    int tempPortNumber;
                    try {
                        tempPortNumber = Integer.parseInt(s);

                        // Check whether we entered a valid port number (inside the range)
                        if (!BackendController.isValidPortNumber(tempPortNumber)) {
                            System.out.println(I18nController.get("console.setup.port.outOfRange"));
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        // Check whether we entered a number
                        System.out.println(I18nController.get("console.setup.port.nan"));
                        continue;
                    }
                    portNumber = tempPortNumber;
                } else {
                    System.exit(0);
                }
            }
        }
        if (askForSecure) {
            // Read security
            System.out.println(I18nController.get("console.setup.ssl"));
            System.out.print("$ ");
            String s = input.nextLine().trim();
            secure = s.equalsIgnoreCase("yes");
        }

        if (Main.connectToDb && sl.getDb() != null && addToDB) {
            try {
                ServerModel server = new ServerModel(ipAddress, portNumber, secure, true);
                sl.getDb().getServerDao().create(server);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            backend = new BackendController(ipAddress, portNumber, secure);

            logger.info(I18nController.get("console.connection.successful"));
            // Loop, allowing the user to send messages to the server
            // Note: We still have our scanner
            System.out.println(I18nController.get("console.command.init"));
            System.out.println();

            // When starting the app, show the user which commands he can call
            handleHelpCommand();

            // Add event listeners, when the server sends something.
            backend.setMessageTextListener(this);
            backend.setMessageErrorListener(this);

            // Initialize user input handler
            handleCommandInput();
        } catch (InvalidIpException | InvalidPortException | IOException e) {
            e.printStackTrace();
        } finally {
            input.close();
        }
    }

    /**
     * Ask for the command to be sent to the server. If it's a known command, execute the corresponding function.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleCommandInput() throws IOException {
        System.out.print("$ ");
        while (input.hasNext()) {
            String line = input.nextLine();
            switch (line) {
                case "CreateLogin":
                    handleCreateLoginCommand();
                    break;
                case "Login":
                    handleLoginCommand();
                    break;
                case "ChangePassword":
                    handleChangePasswordCommand();
                    break;
                case "DeleteLogin":
                    handleDeleteLoginCommand();
                    break;
                case "Logout":
                    handleLogoutCommand();
                    break;
                case "CreateChatroom":
                    handleCreateChatroomCommand();
                    break;
                case "JoinChatroom":
                    handleJoinChatroomCommand();
                    break;
                case "LeaveChatroom":
                    handleLeaveChatroomCommand();
                    break;
                case "DeleteChatroom":
                    handleDeleteChatroomCommand();
                    break;
                case "ListChatrooms":
                    handleListChatroomsCommand();
                    break;
                case "Ping":
                    handlePingCommand();
                    break;
                case "SendMessage":
                    handleSendMessageCommand();
                    break;
                case "UserOnline":
                    handleUserOnlineCommand();
                    break;
                case "ListChatroomUsers":
                    handleListChatroomUsersCommand();
                    break;
                case "Help":
                    handleHelpCommand();
                    break;
                default:
                    backend.sendCommand(line);
                    break;
            }
            System.out.print("$ ");
        }
    }

    /**
     * Ask for information required for the "CreateLogin" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleCreateLoginCommand() throws IOException {
        System.out.println(I18nController.get("console.createLogin.init"));
        boolean valid = false;
        String username = null, password = null, verifyPassword;

        while (!valid) {
            System.out.print(I18nController.get("console.createLogin.username") + " ");
            username = input.nextLine();
            System.out.print(I18nController.get("console.createLogin.password") + " ");
            password = input.nextLine();
            System.out.print(I18nController.get("console.createLogin.password.repeat") + " ");
            verifyPassword = input.nextLine();

            if (password.equals(verifyPassword)) {
                valid = true;
            } else {
                System.out.println(I18nController.get("console.createLogin.password.invalid"));
            }
        }

        if (username != null && password != null) {
            if (backend.sendCreateLogin(username, password)) {
                System.out.println(I18nController.get("console.createLogin.success"));
            } else {
                System.out.println(I18nController.get("console.createLogin.fail"));
            }
        }
    }

    /**
     * Ask for information required for the "Login" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleLoginCommand() throws IOException {
        System.out.println(I18nController.get("console.login.init"));
        String username, password;

        System.out.print(I18nController.get("console.login.username") + " ");
        username = input.nextLine();
        System.out.print(I18nController.get("console.login.password") + " ");
        password = input.nextLine();

        String tokenTemp = backend.sendLogin(username, password);
        if (tokenTemp != null) {
            model.setCurrentLogin(new LoginModel(username, password, tokenTemp));
            System.out.println(I18nController.get("console.login.success"));
        } else {
            System.out.println(I18nController.get("console.login.fail"));
        }
    }

    /**
     * Ask for information required for the "ChangePassword" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleChangePasswordCommand() throws IOException {
        if (model.getCurrentLogin() == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        String newPassword = null, verifyNewPassword;
        boolean valid = false;

        while (!valid) {
            // TODO: Maybe future feature to enter current password first
            System.out.print(I18nController.get("console.changePassword.password") + " ");
            newPassword = input.nextLine();
            System.out.print(I18nController.get("console.changePassword.password.verify") + " ");
            verifyNewPassword = input.nextLine();

            if (newPassword.equals(verifyNewPassword)) {
                valid = true;
            } else {
                System.out.println(I18nController.get("console.changePassword.password.invalid"));
            }
        }

        if (newPassword != null) {
            if (backend.sendChangePassword(model.getCurrentLogin().getToken(), newPassword)) {
                System.out.println(I18nController.get("console.changePassword.success"));
            } else {
                System.out.println(I18nController.get("console.changePassword.fail"));
            }
        }
    }

    /**
     * Ask for information required for the "DeleteLogin" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleDeleteLoginCommand() throws IOException {
        if (model.getCurrentLogin() == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        boolean verify;

        System.out.println(I18nController.get("console.deleteLogin.init"));
        System.out.print("$ ");
        String s = input.nextLine().trim();
        verify = s.equalsIgnoreCase("yes");

        if (verify) {
            if (backend.sendDeleteLogin(model.getCurrentLogin().getToken())) {
                System.out.println(I18nController.get("console.deleteLogin.success"));

                handleLogoutCommand();
            } else {
                System.out.println(I18nController.get("console.deleteLogin.fail"));
            }
        }
    }

    /**
     * Ask for information required for the "Logout" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleLogoutCommand() throws IOException {
        if (model.getCurrentLogin() == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        System.out.println(I18nController.get("console.logout.init"));
        if (backend.sendLogout()) {
            System.out.println(I18nController.get("console.logout.success"));
            model.setCurrentLogin(null);
        } else {
            System.out.println(I18nController.get("console.logout.fail"));
        }
    }

    /**
     * Ask for information required for the "Ping" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handlePingCommand() throws IOException {
        if (model.getCurrentLogin() != null) {
            System.out.println(I18nController.get("console.ping.token.init"));
            if (backend.sendPing(model.getCurrentLogin().getToken())) {
                System.out.println(I18nController.get("console.ping.success"));
            } else {
                System.out.println(I18nController.get("console.ping.fail"));
            }
        } else {
            System.out.println(I18nController.get("console.ping.init"));
            if (backend.sendPing()) {
                System.out.println(I18nController.get("console.ping.success"));
            } else {
                System.out.println(I18nController.get("console.ping.fail"));
            }
        }
    }

    /**
     * Ask for information required for the "CreateChatroom" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleCreateChatroomCommand() throws IOException {
        if (model.getCurrentLogin() == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        String name;
        boolean isPublic;

        System.out.print(I18nController.get("console.createGroupChat.name") + " ");
        name = input.nextLine();

        System.out.print(I18nController.get("console.createGroupChat.public") + " ");
        String s = input.nextLine().trim();
        isPublic = s.equalsIgnoreCase("yes");

        if (backend.sendCreateChatroom(model.getCurrentLogin().getToken(), name, isPublic)) {
            System.out.println(I18nController.get("console.createGroupChat.success"));

            System.out.println(I18nController.get("console.joinChatroom.task"));
            if (backend.sendJoinChatroom(model.getCurrentLogin().getToken(), name, model.getCurrentLogin().getUsername())) {
                System.out.println(I18nController.get("console.joinChatroom.success"));
            } else {
                System.out.println(I18nController.get("console.joinChatroom.fail"));
            }
        } else {
            System.out.println(I18nController.get("console.createGroupChat.fail"));
        }
    }

    /**
     * Ask for information required for the "JoinChatroom" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleJoinChatroomCommand() throws IOException {
        if (model.getCurrentLogin() == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        String roomName = null, username;
        boolean validRoomName = false;

        System.out.println(I18nController.get("console.joinChatroom.init"));
        handleListChatroomsCommand();
        ArrayList<String> chatrooms = backend.sendListChatrooms(model.getCurrentLogin().getToken());
        while (!validRoomName) {
            System.out.print(I18nController.get("console.joinChatroom.name") + " ");
            roomName = input.nextLine();

            if (chatrooms.contains(roomName)) {
                validRoomName = true;
            } else {
                System.out.println(I18nController.get("console.joinChatroom.name.invalid"));
            }
        }

        System.out.print(I18nController.get("console.joinChatroom.user") + " ");
        username = input.nextLine();

        System.out.println(I18nController.get("console.joinChatroom.task"));
        if (backend.sendJoinChatroom(model.getCurrentLogin().getToken(), roomName, username)) {
            System.out.println(I18nController.get("console.joinChatroom.success"));
        } else {
            System.out.println(I18nController.get("console.joinChatroom.fail"));
        }
    }

    /**
     * Ask for information required for the "LeaveChatroom" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleLeaveChatroomCommand() throws IOException {
        if (model.getCurrentLogin() == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        String roomName = null, username;
        boolean validRoomName = false;

        handleListChatroomsCommand();
        ArrayList<String> chatrooms = backend.sendListChatrooms(model.getCurrentLogin().getToken());
        while (!validRoomName) {
            System.out.print(I18nController.get("console.leaveChatroom.name") + " ");
            roomName = input.nextLine();

            if (chatrooms.contains(roomName)) {
                validRoomName = true;
            } else {
                System.out.println(I18nController.get("console.leaveChatroom.name.invalid"));
            }
        }

        System.out.print(I18nController.get("console.leaveChatroom.user") + " ");
        username = input.nextLine();

        if (backend.sendLeaveChatroom(model.getCurrentLogin().getToken(), roomName, username)) {
            System.out.println(I18nController.get("console.leaveChatroom.success"));
        } else {
            System.out.println(I18nController.get("console.leaveChatroom.fail"));
        }
    }

    /**
     * Ask for information required for the "DeleteChatroom" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleDeleteChatroomCommand() throws IOException {
        if (model.getCurrentLogin() == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        String roomName = null;
        boolean validRoomName = false;

        handleListChatroomsCommand();
        ArrayList<String> chatrooms = backend.sendListChatrooms(model.getCurrentLogin().getToken());
        while (!validRoomName) {
            System.out.print(I18nController.get("console.deleteChatroom.name") + " ");
            roomName = input.nextLine();

            if (chatrooms.contains(roomName)) {
                validRoomName = true;
            } else {
                System.out.println(I18nController.get("console.deleteChatroom.name.invalid"));
            }
        }

        if (backend.sendDeleteChatroom(model.getCurrentLogin().getToken(), roomName)) {
            System.out.println(I18nController.get("console.deleteChatroom.success"));
        } else {
            System.out.println(I18nController.get("console.deleteChatroom.fail"));
        }
    }

    /**
     * Ask for information required for the "ListChatrooms" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleListChatroomsCommand() throws IOException {
        if (model.getCurrentLogin() == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        ArrayList<String> chatrooms = backend.sendListChatrooms(model.getCurrentLogin().getToken());
        if (chatrooms == null) {
            System.out.println(I18nController.get("console.listChatrooms.fail"));
        } else if (chatrooms.size() > 0) {
            System.out.println(I18nController.get("console.listChatrooms.list"));
            chatrooms.forEach(s -> System.out.println("* " + s));
        } else {
            System.out.println(I18nController.get("console.listChatrooms.list.empty"));
        }
    }

    /**
     * Ask for information required for the "SendMessage" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleSendMessageCommand() throws IOException {
        if (model.getCurrentLogin() == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        String target = null, message = null;
        boolean validTarget = false, validMessage = false;

        System.out.println(I18nController.get("console.sendMessage.init"));
        handleListChatroomsCommand();
        ArrayList<String> chatrooms = backend.sendListChatrooms(model.getCurrentLogin().getToken());

        while (!validTarget) {
            System.out.print(I18nController.get("console.sendMessage.target") + " ");
            target = input.nextLine();

            // Check if it's a room
            if (chatrooms.contains(target)) {
                validTarget = true;
            } else {
                if (backend.sendUserOnline(model.getCurrentLogin().getToken(), target)) {
                    validTarget = true;
                } else {
                    System.out.println(I18nController.get("console.sendMessage.target.invalid"));
                }
            }
        }

        while (!validMessage) {
            System.out.print(I18nController.get("console.sendMessage.message") + " ");
            message = input.nextLine();

            // Check if it is not more than 1024 characters
            if (message.length() <= 1024) {
                validMessage = true;
            } else {
                System.out.println("console.sendMessage.message.invalid.maxLength");
            }
        }

        if (backend.sendSendMessage(model.getCurrentLogin().getToken(), target, message)) {
            System.out.println(I18nController.get("console.sendMessage.success"));
        } else {
            System.out.println(I18nController.get("console.sendMessage.fail"));
        }
    }

    /**
     * Ask for information required for the "UserOnline" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleUserOnlineCommand() throws IOException {
        if (model.getCurrentLogin() == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        String username;
        System.out.print(I18nController.get("console.userOnline.user") + " ");
        username = input.nextLine();

        if (backend.sendUserOnline(model.getCurrentLogin().getToken(), username)) {
            System.out.println(I18nController.get("console.userOnline.success"));
        } else {
            System.out.println(I18nController.get("console.userOnline.fail"));
        }
    }

    /**
     * Ask for information required for the "ListChatroomUsers" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleListChatroomUsersCommand() throws IOException {
        if (model.getCurrentLogin() == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        String chatroom;
        ArrayList<String> usersInChatroom;

        handleListChatroomsCommand();

        System.out.print(I18nController.get("console.listChatroomUsers.name") + " ");
        chatroom = input.nextLine();

        usersInChatroom = backend.sendListChatroomUsers(model.getCurrentLogin().getToken(), chatroom);
        if (usersInChatroom == null) {
            System.out.println(I18nController.get("console.listChatroomUsers.failed"));
        } else if (usersInChatroom.size() > 0) {
            System.out.println(I18nController.get("console.listChatroomUsers.list", chatroom));
            usersInChatroom.forEach(s -> System.out.println("* " + s));
        } else {
            System.out.println(I18nController.get("console.listChatroomUsers.empty", chatroom));
        }
    }

    private void handleHelpCommand() {
        System.out.println(I18nController.get("console.help.init"));
        Arrays.asList(new String[][]{
            {"CreateLogin       ", I18nController.get("console.help.createLogin")},
            {"Login             ", I18nController.get("console.help.login")},
            {"ChangePassword    ", I18nController.get("console.help.changePassword")},
            {"DeleteLogin       ", I18nController.get("console.help.deleteLogin")},
            {"Logout            ", I18nController.get("console.help.logout")},
            {"CreateChatroom    ", I18nController.get("console.help.createChatroom")},
            {"JoinChatroom      ", I18nController.get("console.help.joinChatroom")},
            {"LeaveChatroom     ", I18nController.get("console.help.leaveChatroom")},
            {"DeleteChatroom    ", I18nController.get("console.help.deleteChatroom")},
            {"ListChatrooms     ", I18nController.get("console.help.listChatrooms")},
            {"Ping              ", I18nController.get("console.help.ping")},
            {"SendMessage       ", I18nController.get("console.help.sendMessage")},
            {"UserOnline        ", I18nController.get("console.help.userOnline")},
            {"ListChatroomUsers ", I18nController.get("console.help.listChatroomUsers")},
            {"Help              ", I18nController.get("console.help.help")}
        }).forEach(s -> {
            Arrays.asList(s).forEach(System.out::print);
            System.out.println();
        });
    }

    @Override
    public void onMessageErrorEvent(String errorMessage) {
        // "Invalid command" is a known value, so give a more informative output
        if (errorMessage.equals("Invalid command")) {
            System.out.println(I18nController.get("console.messageError.command.invalid"));
        } else {
            System.out.println(I18nController.get("console.messageError.default", errorMessage));
        }
    }

    @Override
    public void onMessageTextEvent(UserModel user, ChatModel chat, MessageModel message) {
        System.out.println(I18nController.get("console.messageText.init",
            user.getUsername(),
            chat.getName(),
            message.getMessage()));
    }
}
