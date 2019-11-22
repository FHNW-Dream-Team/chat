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
     * If desired, connect to the local database and use it's information. If nowhere saved, ask for IP address, port
     * and whether to use SSL.
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

            System.out.println(I18nController.get("console.setup.server.select"));
            System.out.println(I18nController.get("console.setup.server.create"));
            for (ServerModel server : db.serverDao) {
                serverCounter++;
                serverList.add(server);
                if (server.isSecure()) {
                    System.out.println(I18nController.get("console.setup.server.entry.ssl", serverCounter, server.getIp(), Integer.toString(server.getPort())));
                } else {
                    System.out.println(I18nController.get("console.setup.server.entry", serverCounter, server.getIp(), Integer.toString(server.getPort())));
                }
            }

            while (!validServer) {
                System.out.print("$ ");
                String chosenServer = input.nextLine();
                int chosenServerInt = Integer.parseInt(chosenServer);

                if (chosenServerInt > serverCounter | chosenServerInt < 0) {
                    System.out.println(I18nController.get("console.setup.server.entry.invalid"));
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
                System.out.println(I18nController.get("console.setup.ip"));
                System.out.print("$ ");
                ipAddress = input.nextLine();
                validIp = BackendController.isValidIpAddress(ipAddress);
            }
        }
        if (portNumber == -1) {
            boolean validPort = false;
            // Read port
            while (!validPort) {
                System.out.println(I18nController.get("console.setup.port"));
                System.out.print("$ ");
                String strPort = input.nextLine();
                portNumber = Integer.parseInt(strPort);
                validPort = BackendController.isValidPortNumber(portNumber);
            }
        }
        if (askForSecure) {
            // Read security
            System.out.println(I18nController.get("console.setup.ssl"));
            System.out.print("$ ");
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

            logger.info(I18nController.get("console.connection.successful"));
            // Loop, allowing the user to send messages to the server
            // Note: We still have our scanner
            System.out.println(I18nController.get("console.command.init"));
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
            login = new LoginModel(username, password, tokenTemp);
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
        if (login == null) {
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
            if (backend.sendChangePassword(login.getToken(), newPassword)) {
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
        if (login == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        boolean verify;

        System.out.println(I18nController.get("console.deleteLogin.init"));
        System.out.print("$ ");
        String s = input.nextLine().trim();
        verify = s.equalsIgnoreCase("yes");

        if (verify) {
            if (backend.sendDeleteLogin(login.getToken())) {
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
        if (login == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        System.out.println(I18nController.get("console.logout.init"));
        if (backend.sendLogout()) {
            System.out.println(I18nController.get("console.logout.success"));
            login = null;
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
        if (login != null) {
            System.out.println(I18nController.get("console.ping.token.init"));
            if (backend.sendPing(login.getToken())) {
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
        if (login == null) {
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

        if (backend.sendCreateChatroom(login.getToken(), name, isPublic)) {
            System.out.println(I18nController.get("console.createGroupChat.success"));

            System.out.println(I18nController.get("console.joinChatroom.task"));
            if (backend.sendJoinChatroom(login.getToken(), name, login.getUsername())) {
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
        if (login == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        String roomName = null, username;
        boolean validRoomName = false;

        System.out.println(I18nController.get("console.joinChatroom.init"));
        handleListChatroomsCommand();
        ArrayList<String> chatrooms = backend.sendListChatrooms(login.getToken());
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
        if (backend.sendJoinChatroom(login.getToken(), roomName, username)) {
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
        if (login == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        String roomName = null, username;
        boolean validRoomName = false;

        handleListChatroomsCommand();
        ArrayList<String> chatrooms = backend.sendListChatrooms(login.getToken());
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

        if (backend.sendLeaveChatroom(login.getToken(), roomName, username)) {
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
        if (login == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        String roomName = null;
        boolean validRoomName = false;

        handleListChatroomsCommand();
        ArrayList<String> chatrooms = backend.sendListChatrooms(login.getToken());
        while (!validRoomName) {
            System.out.print(I18nController.get("console.deleteChatroom.name") + " ");
            roomName = input.nextLine();

            if (chatrooms.contains(roomName)) {
                validRoomName = true;
            } else {
                System.out.println(I18nController.get("console.deleteChatroom.name.invalid"));
            }
        }

        if (backend.sendDeleteChatroom(login.getToken(), roomName)) {
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
        if (login == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        ArrayList<String> chatrooms = backend.sendListChatrooms(login.getToken());
        if (chatrooms == null) {
            System.out.println(I18nController.get("console.listChatrooms.fail"));
        } else if (chatrooms.size() > 0) {
            System.out.println("console.listChatrooms.list");
            chatrooms.forEach(s -> System.out.println("* " + s));
        } else {
            System.out.println("console.listChatrooms.list.empty");
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
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        String target = null, message = null;
        boolean validTarget = false, validMessage = false;

        System.out.println(I18nController.get("console.sendMessage.init"));
        handleListChatroomsCommand();
        ArrayList<String> chatrooms = backend.sendListChatrooms(login.getToken());

        while (!validTarget) {
            System.out.print(I18nController.get("console.sendMessage.target") + " ");
            target = input.nextLine();

            // Check if it's a room
            if (chatrooms.contains(target)) {
                validTarget = true;
            } else {
                if (backend.sendUserOnline(login.getToken(), target)) {
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

        if (backend.sendSendMessage(login.getToken(), target, message)) {
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
        if (login == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        String username;
        System.out.print(I18nController.get("console.userOnline.user") + " ");
        username = input.nextLine();

        if (backend.sendUserOnline(login.getToken(), username)) {
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
        if (login == null) {
            System.out.println(I18nController.get("console.login.required"));
            return;
        }

        String chatroom;
        ArrayList<String> usersInChatroom;

        handleListChatroomsCommand();

        System.out.print(I18nController.get("console.listChatroomUsers.name") + " ");
        chatroom = input.nextLine();

        usersInChatroom = backend.sendListChatroomUsers(login.getToken(), chatroom);
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
        Arrays.asList(new String[]{
            "CreateLogin",
            "Login",
            "ChangePassword",
            "DeleteLogin",
            "Logout",
            "CreateChatroom",
            "JoinChatroom",
            "LeaveChatroom",
            "DeleteChatroom",
            "ListChatrooms",
            "Ping",
            "SendMessage",
            "UserOnline",
            "ListChatroomUsers",
            "Help"
        }).forEach(s -> System.out.print("* " + s));
        System.out.println();
    }
}
