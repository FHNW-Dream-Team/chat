package com.orbitrondev.Controller;

import com.orbitrondev.Exception.InvalidIpException;
import com.orbitrondev.Exception.InvalidPortException;
import com.orbitrondev.Main;
import com.orbitrondev.Model.MainModel;
import com.orbitrondev.Model.ServerModel;
import com.orbitrondev.View.MainView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;

public class CliController {
    private MainModel model;

    private BackendController backend;
    private DatabaseController db;

    public CliController(MainModel model) {
        this.model = model;

        String ipAddress = null;
        int portNumber = -1;
        boolean secure = true;
        boolean askForSecure = true;

        if (Main.connectToDb) {
            db = new DatabaseController("chat.sqlite");
            for (ServerModel server : db.serverDao) {
                if (server.isDefaultServer()) {
                    ipAddress = server.getIp();
                    portNumber = server.getPort();
                    secure = server.isSecure();
                    askForSecure = false;
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

        try {
            backend = new BackendController(ipAddress, portNumber, secure);

            System.out.println("Connected");
            // Loop, allowing the user to send messages to the server
            // Note: We still have our scanner
            System.out.println("Enter commands to server or ctrl-D to quit");

            while (in.hasNext()) {
                String line = in.nextLine();
                backend.sendCommand(line);
            }
        } catch (InvalidIpException | InvalidPortException | IOException e) {
            e.printStackTrace();
        }
    }
}
