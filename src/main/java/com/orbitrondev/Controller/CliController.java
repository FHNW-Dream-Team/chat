package com.orbitrondev.Controller;

import com.orbitrondev.Exception.InvalidIpException;
import com.orbitrondev.Exception.InvalidPortException;
import com.orbitrondev.Main;
import com.orbitrondev.Model.LoginModel;
import com.orbitrondev.Model.MainModel;
import com.orbitrondev.Model.ServerModel;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class CliController {
    private MainModel model;

    private BackendController backend;
    private DatabaseController db;

    private LoginModel login = null;

    public CliController(MainModel model) {
        this.model = model;

        String ipAddress = null;
        int portNumber = -1;
        boolean secure = false;
        boolean askForSecure = true;
        boolean addToDB = true;

        if (Main.connectToDb) {
            db = new DatabaseController(Main.dbLocation);
            for (ServerModel server : db.serverDao) {
                if (server.isDefaultServer()) {
                    ipAddress = server.getIp();
                    portNumber = server.getPort();
                    secure = server.isSecure();
                    askForSecure = false;
                    addToDB = false;
                }
            }
        }

        Scanner in = new Scanner(System.in);
        if (ipAddress == null) {
            boolean validIp = false;

            // Read IP address
            while (!validIp) {
                System.out.println("Enter the address of the server");
                ipAddress = in.nextLine();
                validIp = BackendController.isValidIpAddress(ipAddress);
            }
        }
        if (portNumber == -1) {
            boolean validPort = false;
            // Read port
            while (!validPort) {
                System.out.println("Enter the port number on the server (1024-65535)");
                String strPort = in.nextLine();
                portNumber = Integer.parseInt(strPort);
                validPort = BackendController.isValidPortNumber(portNumber);
            }
        }
        if (askForSecure) {
            // Read security
            System.out.println("Enter 'yes' if the client should use SecureSockets");
            String s = in.nextLine().trim();
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

            System.out.println("Connected");
            // Loop, allowing the user to send messages to the server
            // Note: We still have our scanner
            System.out.println("Enter commands to server or ctrl-D to quit");

            handleCommandInput(in);
        } catch (InvalidIpException | InvalidPortException | IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCommandInput(Scanner in) throws IOException {
        while (in.hasNext()) {
            String line = in.nextLine();
            switch (line) {
                case "CreateLogin":
                    handleCreateLoginCommand(in);
                    break;
                case "Login":
                    handleLoginCommand(in);
                    break;
                case "ChangePassword":
                    handleChangePasswordCommand(in);
                    break;
                case "DeleteLogin":
                    handleDeleteLoginCommand(in);
                    break;
                case "Logout":
                    handleLogoutCommand(in);
                    break;
                case "CreateChatroom":
                    handleCreateChatroomCommand(in);
                    break;
                case "JoinChatroom":
                    handleJoinChatroomCommand(in);
                    break;
                case "LeaveChatroom":
                    handleLeaveChatroomCommand(in);
                    break;
                case "DeleteChatroom":
                    handleDeleteChatroomCommand(in);
                    break;
                case "ListChatrooms":
                    handleListChatroomsCommand(in);
                    break;
                case "Ping":
                    handlePingCommand(in);
                    break;
                case "SendMessage":
                    handleSendMessageCommand(in);
                    break;
                case "UserOnline":
                    handleUserOnlineCommand(in);
                    break;
                case "ListChatroomUsers":
                    handleListChatroomUsersCommand(in);
                    break;
                default:
                    backend.sendCommand(line);
                    break;
            }
        }
    }

    private void handleCreateLoginCommand(Scanner in) throws IOException {
        System.out.println("To create a new user we need: username, password");
        boolean valid = false;
        String username = null, password = null, verifyPassword = null;

        while (!valid) {
            System.out.print("Username: ");
            username = in.nextLine();
            System.out.print("Password: ");
            password = in.nextLine();
            System.out.print("Repeat Password: ");
            verifyPassword = in.nextLine();

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

    private void handleLoginCommand(Scanner in) throws IOException {
        System.out.println("To login you need to have created a user");
        String username, password;

        System.out.print("Username: ");
        username = in.nextLine();
        System.out.print("Password: ");
        password = in.nextLine();

        String tokenTemp = backend.sendLogin(username, password);
        if (tokenTemp != null) {
            login = new LoginModel(username, password, tokenTemp);
            System.out.println("Successfully logged in");
        } else {
            System.out.println("Not logged in");
        }
    }

    private void handleChangePasswordCommand(Scanner in) throws IOException {
        if (login == null) {
            System.out.println("To do this action, you need to be logged in!");
            return;
        }

        String newPassword = null, verifyNewPassword = null;
        boolean valid = false;

        while (!valid) {
            // TODO: Maybe future feature to enter current password first
            System.out.print("New password: ");
            newPassword = in.nextLine();
            System.out.print("Verify new password: ");
            verifyNewPassword = in.nextLine();

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

    private void handleDeleteLoginCommand(Scanner in) throws IOException {
        if (login == null) {
            System.out.println("To do this action, you need to be logged in!");
            return;
        }

        boolean verify;

        System.out.println("Are you sure you want to delete this account? If so, enter \"yes\".");
        String s = in.nextLine().trim();
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

    private void handleLogoutCommand(Scanner in) throws IOException {
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

    private void handlePingCommand(Scanner in) throws IOException {
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

    private void handleCreateChatroomCommand(Scanner in) throws IOException {
        if (login == null) {
            System.out.println("To do this action, you need to be logged in!");
            return;
        }

        String name;
        boolean isPublic = false;

        System.out.print("Chatroom name: ");
        name = in.nextLine();

        System.out.print("Should the room be accessible publicly? If so, enter \"yes\"");
        String s = in.nextLine().trim();
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

    private void handleJoinChatroomCommand(Scanner in) throws IOException {
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
            roomName = in.nextLine();

            if (chatrooms.contains(roomName)) {
                validRoomName = true;
            } else {
                System.out.println("The given room name is invalid. Retry!");
            }
        }

        System.out.print("Username: ");
        username = in.nextLine();

        if (backend.sendJoinChatroom(login.getToken(), roomName, username)) {
            System.out.println("Room joined successfully");
        } else {
            System.out.println("Room was not joined");
        }
    }

    private void handleLeaveChatroomCommand(Scanner in) throws IOException {
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
            roomName = in.nextLine();

            if (chatrooms.contains(roomName)) {
                validRoomName = true;
            } else {
                System.out.println("The given room name is invalid. Retry!");
            }
        }

        System.out.print("Username: ");
        username = in.nextLine();

        if (backend.sendLeaveChatroom(login.getToken(), roomName, username)) {
            System.out.println("Room left successfully");
        } else {
            System.out.println("Room was not left");
        }
    }

    private void handleDeleteChatroomCommand(Scanner in) throws IOException {
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
            roomName = in.nextLine();

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

    private void handleListChatroomsCommand(Scanner in) throws IOException {
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

    private void handleSendMessageCommand(Scanner in) throws IOException {
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
            target = in.nextLine();

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
            message = in.nextLine();

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

    private void handleUserOnlineCommand(Scanner in) throws IOException {
        if (login == null) {
            System.out.println("To do this action, you need to be logged in!");
            return;
        }

        String username = null;
        System.out.print("Username: ");
        username = in.nextLine();

        if (backend.sendUserOnline(login.getToken(), username)) {
            System.out.println("The user is online");
        } else {
            System.out.println("The given room name/username is invalid. Retry!");
        }
    }

    private void handleListChatroomUsersCommand(Scanner in) throws IOException {
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
        chatroom = in.nextLine();

        usersInChatroom = backend.sendListChatroomUsers(login.getToken(), chatroom);
        System.out.println("Following users are inside the room " + chatroom + ":");
        usersInChatroom.forEach(s -> System.out.println("* " + s));
    }
}
