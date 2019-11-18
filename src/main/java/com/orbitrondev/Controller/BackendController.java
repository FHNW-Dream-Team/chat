package com.orbitrondev.Controller;

import com.orbitrondev.Exception.InvalidIpException;
import com.orbitrondev.Exception.InvalidPortException;
import com.orbitrondev.Model.UserModel;
import com.sun.net.ssl.internal.ssl.Provider;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;

public class BackendController implements Closeable {

    private Socket socket;

    private BufferedReader socketIn;
    private OutputStreamWriter socketOut;

    private ArrayList<String> lastMessage = new ArrayList<>();

    public BackendController(String ipAddress, int port) throws InvalidIpException, InvalidPortException {
        if (!isValidIpAddress(ipAddress)) {
            throw new InvalidIpException();
        }
        if (!isValidPortNumber(port)) {
            throw new InvalidPortException();
        }

        try {
            createStandardSocket(ipAddress, port);

            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketOut = new OutputStreamWriter(socket.getOutputStream());

            createResponseThread();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public BackendController(String ipAddress, int port, boolean secure) throws InvalidIpException, InvalidPortException {
        if (!isValidIpAddress(ipAddress)) {
            throw new InvalidIpException();
        }
        if (!isValidPortNumber(port)) {
            throw new InvalidPortException();
        }

        try {
            if (secure) {
                createSecureSocket(ipAddress, port);
            } else {
                createStandardSocket(ipAddress, port);
            }

            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketOut = new OutputStreamWriter(socket.getOutputStream());

            createResponseThread();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void createResponseThread() {
        // Create thread to read incoming messages
        Runnable r = () -> {
            while (true) {
                String msg;
                String[] msgSplit;
                try {
                    msg = socketIn.readLine();
                    System.out.println("Received: " + msg);

                    if (msg != null && msg.length() > 0) {
                        msgSplit = msg.split("\\|");
                        lastMessage.addAll(Arrays.asList(msgSplit));

                        switch (lastMessage.get(0)) {
                            case "MessageText":
                                receivedMessageText(lastMessage.get(1), lastMessage.get(2), lastMessage.get(3));
                                break;
                            case "MessageError":
                                receivedMessageError(lastMessage.get(1));
                                break;
                        }
                    }
                } catch (IOException e) {
                    break;
                }
                if (msg == null) break; // In case the server closes the socket
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    private void receivedMessageError(String errorMessage) {
        // TODO: Handle if we send wrong commands.
    }

    private void receivedMessageText(String username, String targetChat, String text) {
        // TODO: Handle incoming new messages.
    }

    private void waitForResultResponse() {
        // TODO: This is somehow the reason the tests fail
        while (lastMessage.size() == 0 || !lastMessage.get(0).equals("Result")) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // We don't care
                e.printStackTrace();
            }
        }
    }

    public void sendCommand(String command) throws IOException {
        System.out.println("Sent: " + command);
        socketOut.write(command + "\n");
        socketOut.flush();
    }

    public void sendCommand(String[] commandParts) throws IOException {
        sendCommand(String.join("|", commandParts));
    }

    /**
     * Fails if name already taken (user or chatroom), or invalid
     * After creating an account, you still have to login
     */
    public boolean sendCreateLogin(String username, String password) throws IOException {
        sendCommand(new String[]{"CreateLogin", username, password});

        waitForResultResponse();
        boolean result = Boolean.parseBoolean(lastMessage.get(1));
        lastMessage.clear();
        return result;
    }

    /**
     * Fails if name/password do not match
     */
    public String sendLogin(String username, String password) throws IOException {
        sendCommand(new String[]{"Login", username, password});

        waitForResultResponse();
        if (lastMessage.get(1).equals("true")) {
            String token = lastMessage.get(2);
            lastMessage.clear();
            return token;
        }
        lastMessage.clear();
        return null;
    }

    /**
     * Fails only if token is invalid
     */
    public boolean sendChangePassword(String token, String newPassword) throws IOException {
        sendCommand(new String[]{"ChangePassword", token, newPassword});

        waitForResultResponse();
        boolean result = Boolean.parseBoolean(lastMessage.get(1));
        lastMessage.clear();
        return result;
    }

    /**
     * Fails only if token is invalid; after delete, token becomes invalid
     */
    public boolean sendDeleteLogin(String token) throws IOException {
        sendCommand(new String[]{"DeleteLogin", token});

        waitForResultResponse();
        boolean result = Boolean.parseBoolean(lastMessage.get(1));
        lastMessage.clear();
        return result;
    }

    /**
     * Never fails; token becomes invalid
     */
    public boolean sendLogout() throws IOException {
        sendCommand(new String[]{"Logout"});

        waitForResultResponse();
        boolean result = Boolean.parseBoolean(lastMessage.get(1));
        lastMessage.clear();
        return result;
    }

    /**
     * Fails if name already taken (user or chatroom), or invalid
     * After creating a chatroom, you still have to join
     */
    public boolean sendCreateChatroom(String token, String name, boolean isPublic) throws IOException {
        sendCommand(new String[]{"CreateChatroom", token, name, (isPublic ? "true" : "false")});

        waitForResultResponse();
        boolean result = Boolean.parseBoolean(lastMessage.get(1));
        lastMessage.clear();
        return result;
    }

    /**
     * User can add themselves to public chatrooms
     * Only the creator can add user to a private chatroom
     */
    public boolean sendJoinChatroom(String token, String chatroom, String username) throws IOException {
        sendCommand(new String[]{"JoinChatroom", token, chatroom, username});

        waitForResultResponse();
        boolean result = Boolean.parseBoolean(lastMessage.get(1));
        lastMessage.clear();
        return result;
    }

    public boolean sendJoinChatroom(String token, String chatroom, UserModel user) throws IOException {
        return sendJoinChatroom(token, chatroom, user.getUsername());
    }

    /**
     * You can always remove yourself
     * Chatroom creator can remove anyone
     */
    public boolean sendLeaveChatroom(String token, String chatroom, String username) throws IOException {
        sendCommand(new String[]{"LeaveChatroom", token, chatroom, username});

        waitForResultResponse();
        boolean result = Boolean.parseBoolean(lastMessage.get(1));
        lastMessage.clear();
        return result;
    }

    public boolean sendLeaveChatroom(String token, String chatroom, UserModel user) throws IOException {
        return sendLeaveChatroom(token, chatroom, user.getUsername());
    }

    /**
     * Only the creator can delete a chatroom
     */
    public boolean sendDeleteChatroom(String token, String chatroom) throws IOException {
        sendCommand(new String[]{"DeleteChatroom", token, chatroom});

        waitForResultResponse();
        boolean result = Boolean.parseBoolean(lastMessage.get(1));
        lastMessage.clear();
        return result;
    }

    /**
     * Returns a list of all public chatrooms
     */
    public ArrayList<String> sendListChatrooms(String token) throws IOException {
        sendCommand(new String[]{"ListChatrooms", token});

        waitForResultResponse();
        if (lastMessage.get(1).equals("true")) {
            lastMessage.remove("Result");
            lastMessage.remove("true");
            ArrayList<String> chatroomList = new ArrayList<>(lastMessage);

            lastMessage.clear();
            return chatroomList;
        }
        lastMessage.clear();
        return null;
    }

    /**
     * Without a token: always succeeds
     * With token: succeeds only if token is valid
     */
    public boolean sendPing() throws IOException {
        sendCommand(new String[]{"Ping"});

        waitForResultResponse();
        boolean result = Boolean.parseBoolean(lastMessage.get(1));
        lastMessage.clear();
        return result;
    }

    public boolean sendPing(String token) throws IOException {
        sendCommand(new String[]{"Ping", token});

        waitForResultResponse();
        boolean result = Boolean.parseBoolean(lastMessage.get(1));
        lastMessage.clear();
        return result;
    }

    /**
     * Send message to user or chatroom
     * Fails if user not online / Fails if not a member of the chatroom
     */
    public boolean sendSendMessage(String token, String target, String message) throws IOException {
        sendCommand(new String[]{"SendMessage", token, target, message});

        waitForResultResponse();
        boolean result = Boolean.parseBoolean(lastMessage.get(1));
        lastMessage.clear();
        return result;
    }

    /**
     * Succeeds if the user is currently logged in
     */
    public boolean sendUserOnline(String token, String username) throws IOException {
        sendCommand(new String[]{"UserOnline", token, username});

        waitForResultResponse();
        boolean result = Boolean.parseBoolean(lastMessage.get(1));
        lastMessage.clear();
        return result;
    }

    public boolean sendUserOnline(String token, UserModel user) throws IOException {
        return sendUserOnline(token, user.getUsername());
    }

    /**
     * Returns a list of all users in the given chatroom
     * You must be a member of this chatroom
     */
    public ArrayList<String> sendListChatroomUsers(String token, String chatroom) throws IOException {
        sendCommand(new String[]{"ListChatroomUsers", token, chatroom});

        waitForResultResponse();
        if (lastMessage.get(1).equals("true")) {
            lastMessage.remove("Result");
            lastMessage.remove("true");
            ArrayList<String> usersList = new ArrayList<>(lastMessage);

            lastMessage.clear();
            return usersList;
        }
        lastMessage.clear();
        return null;
    }

    public void createStandardSocket(String ipAddress, int port) throws IOException {
        socket = new Socket(ipAddress, port);
    }

    public void createSecureSocket(String ipAddress, int port) throws IOException {
        // TODO: SSL is not properly setup
        // Check out: https://gitlab.fhnw.ch/bradley.richards/java-projects/blob/master/src/chatroom/Howto_SSL_Certificates_in_Java.odt

        // Registering the JSSE provider
        Security.addProvider(new Provider());

        // Specifying the Truststore details. This is needed if you have created a
        // truststore, for example, for self-signed certificates
        System.setProperty("javax.net.ssl.trustStore", "truststore.ts");
        System.setProperty("javax.net.ssl.trustStorePassword", "trustme");

        // Creating Client Sockets
        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        socket = sslsocketfactory.createSocket(ipAddress, port);

        // The next line is entirely optional !!
        // The SSL handshake would happen automatically, the first time we send data.
        // Or we can immediately force the handshaking with this method:
        ((SSLSocket) socket).startHandshake();
    }

    /**
     * Reference: https://stackoverflow.com/questions/5667371/validate-ipv4-address-in-java
     */
    public static boolean isValidIpAddress(String ipAddress) {
        return ipAddress.matches("^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$");
    }

    public static boolean isValidPortNumber(int port) {
        return port >= 1024 && port <= 65535;
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }
}
