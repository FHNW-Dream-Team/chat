package com.orbitrondev.Controller;

import com.orbitrondev.Exception.InvalidIpException;
import com.orbitrondev.Exception.InvalidPortException;
import com.orbitrondev.Main;
import com.orbitrondev.Model.LoginModel;
import com.orbitrondev.Model.MainModel;
import com.orbitrondev.Model.ServerModel;
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
public class CliController {

    private static final Logger logger = LogManager.getLogger(BackendController.class);

    private MainModel model;

    private BackendController backend;
    private DatabaseController db;

    private LoginModel login = null;

    private Scanner input;

    /**
     * If desired, connect to the local database and use it's information.
     * If nowhere saved, ask for IP address, port and whether to use SSL.
     *
     * @param model An object containing the model to access all data and DB.
     *
     * @since 0.0.1
     */
    public CliController(MainModel model) {
        this.model = model;

        String ipAddress = null;
        int portNumber = -1;
        boolean secure = false;
        boolean askForSecure = true;
        boolean addToDB = true;

        input = new Scanner(System.in);

        if (Main.connectToDb) {
            db = new DatabaseController(Main.dbLocation);

            int serverCounter = 0;
            ArrayList<ServerModel> serverList = new ArrayList<>();
            boolean validServer = false;

            System.out.println("Choose one of the following servers, or enter 0 to create a new one:");
            System.out.println(serverCounter + ") Create new connection...");
            for (ServerModel server : db.serverDao) {
                serverCounter++;
                serverList.add(server);
                System.out.println(serverCounter + ") " + server.getIp() + ":" + server.getPort() + (server.isSecure() ? " (SSL)" : ""));
            }

            while (!validServer) {
                System.out.print("$ ");
                String chosenServer = input.nextLine();
                int chosenServerInt = Integer.parseInt(chosenServer);

                if (chosenServerInt > serverCounter | chosenServerInt < 0) {
                    System.out.println("That server config does not exist. Retry!");
                } else if (chosenServerInt == 0) {
                    validServer = true;
                } else {
                    chosenServerInt--; // The array starts at 0, but the output starts at 1
                    validServer = true;
                    ipAddress = serverList.get(chosenServerInt).getIp();
                    portNumber = serverList.get(chosenServerInt).getPort();
                    secure = serverList.get(chosenServerInt).isSecure();
                    askForSecure = false;
                    addToDB = false;
                }
            }
        }

        if (ipAddress == null) {
            boolean validIp = false;

            // Read IP address
            while (!validIp) {
                System.out.println("Enter the address of the server");
                ipAddress = input.nextLine();
                validIp = BackendController.isValidIpAddress(ipAddress);
            }
        }
        if (portNumber == -1) {
            boolean validPort = false;
            // Read port
            while (!validPort) {
                System.out.println("Enter the port number on the server (1024-65535)");
                String strPort = input.nextLine();
                portNumber = Integer.parseInt(strPort);
                validPort = BackendController.isValidPortNumber(portNumber);
            }
        }
        if (askForSecure) {
            // Read security
            System.out.println("Enter 'yes' if the client should use SecureSockets");
            String s = input.nextLine().trim();
            secure = s.equalsIgnoreCase("yes");
        }

        if (Main.connectToDb && db != null && addToDB) {
            try {
                ServerModel server = new ServerModel(ipAddress, portNumber, secure, true);
                db.serverDao.create(server);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            backend = new BackendController(ipAddress, portNumber, secure);

            logger.info("Connected to server");
            // Loop, allowing the user to send messages to the server
            // Note: We still have our scanner
            System.out.println("Enter commands to server or ctrl-D to quit");
            System.out.println();
            handleHelpCommand();

            handleCommandInput();
        } catch (InvalidIpException | InvalidPortException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ask for the command to be sent to the server. If it's a known command, execute the corresponding function.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleCommandInput() throws IOException {
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
                default:
                    backend.sendCommand(line);
                    break;
            }
        }
    }

    /**
     * Ask for information required for the "CreateLogin" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleCreateLoginCommand() throws IOException {
        System.out.println("To create a new user we need: username, password");
        boolean valid = false;
        String username = null, password = null, verifyPassword = null;

        while (!valid) {
            System.out.print("Username: ");
            username = input.nextLine();
            System.out.print("Password: ");
            password = input.nextLine();
            System.out.print("Repeat Password: ");
            verifyPassword = input.nextLine();

            if (password.equals(verifyPassword)) {
                valid = true;
            } else {
                System.out.println("Passwords do not match");
            }
        }

        if (username != null && password != null) {
            if (backend.sendCreateLogin(username, password)) {
                System.out.println("User was created");
            } else {
                System.out.println("User not created");
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
        System.out.println("To login you need to have created a user");
        String username, password;

        System.out.print("Username: ");
        username = input.nextLine();
        System.out.print("Password: ");
        password = input.nextLine();

        String tokenTemp = backend.sendLogin(username, password);
        if (tokenTemp != null) {
            login = new LoginModel(username, password, tokenTemp);
            System.out.println("Successfully logged in");
        } else {
            System.out.println("Not logged in");
        }
    }

    /**
     * Ask for information required for the "ChangePassword" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleChangePasswordCommand() throws IOException {
        if (login == null) {
            System.out.println("To do this action, you need to be logged in!");
            return;
        }

        String newPassword = null, verifyNewPassword = null;
        boolean valid = false;

        while (!valid) {
            // TODO: Maybe future feature to enter current password first
            System.out.print("New password: ");
            newPassword = input.nextLine();
            System.out.print("Verify new password: ");
            verifyNewPassword = input.nextLine();

            if (newPassword.equals(verifyNewPassword)) {
                valid = true;
            } else {
                System.out.println("Passwords do not match");
            }
        }

        if (newPassword != null) {
            if (backend.sendChangePassword(login.getToken(), newPassword)) {
                System.out.println("Password was changed");
            } else {
                System.out.println("Password was NOT changed");
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
        if (login == null) {
            System.out.println("To do this action, you need to be logged in!");
            return;
        }

        boolean verify;

        System.out.println("Are you sure you want to delete this account? If so, enter \"yes\".");
        String s = input.nextLine().trim();
        verify = s.equalsIgnoreCase("yes");

        if (verify) {
            if (backend.sendDeleteLogin(login.getToken())) {
                System.out.println("User was successfully deleted");

                System.out.println("Logging out...");
                if (backend.sendLogout()) {
                    System.out.println("Logged out");
                } else {
                    System.out.println("Not logged out");
                }
            } else {
                System.out.println("User was not deleted");
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
        if (login == null) {
            System.out.println("To do this action, you need to be logged in!");
            return;
        }

        if (backend.sendLogout()) {
            System.out.println("Logged out");
            login = null;
        } else {
            System.out.println("Not logged out");
        }
    }

    /**
     * Ask for information required for the "Ping" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handlePingCommand() throws IOException {
        if (login != null) {
            System.out.println("Sending ping with token...");
            if (backend.sendPing(login.getToken())) {
                System.out.println("Ping was successful");
            } else {
                System.out.println("Ping was NOT successful");
            }
        } else {
            System.out.println("Sending ping...");
            if (backend.sendPing()) {
                System.out.println("Ping was successful");
            } else {
                System.out.println("Ping was NOT successful");
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
        if (login == null) {
            System.out.println("To do this action, you need to be logged in!");
            return;
        }

        String name;
        boolean isPublic = false;

        System.out.print("Chatroom name: ");
        name = input.nextLine();

        System.out.print("Should the room be accessible publicly? If so, enter \"yes\"");
        String s = input.nextLine().trim();
        isPublic = s.equalsIgnoreCase("yes");

        if (backend.sendCreateChatroom(login.getToken(), name, isPublic)) {
            System.out.println("Chatroom was created");

            System.out.println("Joining the room...");
            if (backend.sendJoinChatroom(login.getToken(), name, login.getUsername())) {
                System.out.println("Room joined successfully");
            } else {
                System.out.println("Room was not joined");
            }
        } else {
            System.out.println("Chatroom was not created");
        }
    }

    /**
     * Ask for information required for the "JoinChatroom" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleJoinChatroomCommand() throws IOException {
        if (login == null) {
            System.out.println("To do this action, you need to be logged in!");
            return;
        }

        String roomName = null, username = null;
        boolean validRoomName = false;

        ArrayList<String> chatrooms = backend.sendListChatrooms(login.getToken());
        System.out.println("Enter the room name you want to enter. Following are available:");
        chatrooms.forEach(s -> System.out.println("* " + s));
        while (!validRoomName) {
            System.out.print("Room name: ");
            roomName = input.nextLine();

            if (chatrooms.contains(roomName)) {
                validRoomName = true;
            } else {
                System.out.println("The given room name is invalid. Retry!");
            }
        }

        System.out.print("Username: ");
        username = input.nextLine();

        if (backend.sendJoinChatroom(login.getToken(), roomName, username)) {
            System.out.println("Room joined successfully");
        } else {
            System.out.println("Room was not joined");
        }
    }

    /**
     * Ask for information required for the "LeaveChatroom" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleLeaveChatroomCommand() throws IOException {
        if (login == null) {
            System.out.println("To do this action, you need to be logged in!");
            return;
        }

        String roomName = null, username = null;
        boolean validRoomName = false;

        ArrayList<String> chatrooms = backend.sendListChatrooms(login.getToken());
        System.out.println("Enter the room name you want to enter. Following are available:");
        chatrooms.forEach(s -> System.out.println("* " + s));
        while (!validRoomName) {
            System.out.print("Room name: ");
            roomName = input.nextLine();

            if (chatrooms.contains(roomName)) {
                validRoomName = true;
            } else {
                System.out.println("The given room name is invalid. Retry!");
            }
        }

        System.out.print("Username: ");
        username = input.nextLine();

        if (backend.sendLeaveChatroom(login.getToken(), roomName, username)) {
            System.out.println("Room left successfully");
        } else {
            System.out.println("Room was not left");
        }
    }

    /**
     * Ask for information required for the "DeleteChatroom" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleDeleteChatroomCommand() throws IOException {
        if (login == null) {
            System.out.println("To do this action, you need to be logged in!");
            return;
        }

        String roomName = null, username = null;
        boolean validRoomName = false;

        ArrayList<String> chatrooms = backend.sendListChatrooms(login.getToken());
        System.out.println("Enter the room name you want to enter. Following are available:");
        chatrooms.forEach(s -> System.out.println("* " + s));
        while (!validRoomName) {
            System.out.print("Room name: ");
            roomName = input.nextLine();

            if (chatrooms.contains(roomName)) {
                validRoomName = true;
            } else {
                System.out.println("The given room name is invalid. Retry!");
            }
        }

        if (backend.sendDeleteChatroom(login.getToken(), roomName)) {
            System.out.println("Room deleted successfully");
        } else {
            System.out.println("Room was not deleted");
        }
    }

    /**
     * Ask for information required for the "ListChatrooms" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleListChatroomsCommand() throws IOException {
        if (login == null) {
            System.out.println("To do this action, you need to be logged in!");
            return;
        }

        ArrayList<String> chatrooms = backend.sendListChatrooms(login.getToken());
        System.out.println("Following chatrooms are available:");
        if (chatrooms != null) {
            chatrooms.forEach(s -> System.out.println("* " + s));
        } else {
            System.out.println("Couldn't get chatrooms");
        }
    }

    /**
     * Ask for information required for the "SendMessage" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleSendMessageCommand() throws IOException {
        if (login == null) {
            System.out.println("To do this action, you need to be logged in!");
            return;
        }

        String target = null, message = null;
        boolean validTarget = false, validMessage = false;

        ArrayList<String> chatrooms = backend.sendListChatrooms(login.getToken());
        System.out.println("Enter the room name or the username you want to message. Following chatrooms are available:");
        chatrooms.forEach(s -> System.out.println("* " + s));

        while (!validTarget) {
            System.out.print("Room name/Username: ");
            target = input.nextLine();

            // Check if it's a room
            if (chatrooms.contains(target)) {
                validTarget = true;
            } else {
                if (backend.sendUserOnline(login.getToken(), target)) {
                    validTarget = true;
                } else {
                    System.out.println("The given room name/username is invalid. Retry!");
                }
            }
        }

        while (!validMessage) {
            System.out.print("Message (max. 1024 characters): ");
            message = input.nextLine();

            // Check if it is not more than 1024 characters
            if (message.length() <= 1024) {
                validMessage = true;
            } else {
                System.out.println("The message is too long. Retry!");
            }
        }

        if (backend.sendSendMessage(login.getToken(), target, message)) {
            System.out.println("Message sent successfully");
        } else {
            System.out.println("Message was not sent");
        }
    }

    /**
     * Ask for information required for the "UserOnline" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleUserOnlineCommand() throws IOException {
        if (login == null) {
            System.out.println("To do this action, you need to be logged in!");
            return;
        }

        String username = null;
        System.out.print("Username: ");
        username = input.nextLine();

        if (backend.sendUserOnline(login.getToken(), username)) {
            System.out.println("The user is online");
        } else {
            System.out.println("The given room name/username is invalid. Retry!");
        }
    }

    /**
     * Ask for information required for the "ListChatroomUsers" command and send it to the server.
     *
     * @throws IOException If an I/O error occurs.
     * @since 0.0.1
     */
    private void handleListChatroomUsersCommand() throws IOException {
        if (login == null) {
            System.out.println("To do this action, you need to be logged in!");
            return;
        }

        String chatroom = null;
        ArrayList<String> usersInChatroom = null;

        ArrayList<String> chatrooms = backend.sendListChatrooms(login.getToken());
        System.out.println("Following chatrooms are available:");
        if (chatrooms != null) {
            chatrooms.forEach(s -> System.out.println("* " + s));
        } else {
            System.out.println("Couldn't get chatrooms");
        }

        System.out.print("Chatroom: ");
        chatroom = input.nextLine();

        usersInChatroom = backend.sendListChatroomUsers(login.getToken(), chatroom);
        System.out.println("Following users are inside the room " + chatroom + ":");
        usersInChatroom.forEach(s -> System.out.println("* " + s));
    }

    private void handleHelpCommand() {
        System.out.println("Following commands are available:");
        Arrays.asList(new String[]{"CreateLogin", "Login", "ChangePassword", "DeleteLogin", "Logout", "CreateChatroom",
            "JoinChatroom", "LeaveChatroom", "DeleteChatroom", "ListChatrooms", "Ping", "SendMessage", "UserOnline",
            "ListChatroomUsers", "Help"})
            .forEach(s -> System.out.print(" * " + s));
        System.out.println();
    }
}
