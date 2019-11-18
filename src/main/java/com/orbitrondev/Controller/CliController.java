package com.orbitrondev.Controller;

import com.orbitrondev.Exception.InvalidIpException;
import com.orbitrondev.Exception.InvalidPortException;
import com.orbitrondev.Model.MainModel;
import com.orbitrondev.Model.ServerModel;
import com.orbitrondev.View.MainView;

import java.util.Scanner;

public class CliController {
    private MainModel model;

    private BackendController backend;
    private DatabaseController db;

    public CliController(MainModel model) {
        this.model = model;
        db = new DatabaseController("chat.sqlite");

        String ipAddress = null;
        int portNumber = -1;
        for (ServerModel server : db.serverDao) {
            if (server.isDefaultServer()) {
                ipAddress = server.getIp();
                portNumber = server.getPort();
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

        try {
            backend = new BackendController(ipAddress, portNumber);
        } catch (InvalidIpException | InvalidPortException e) {
            e.printStackTrace();
        }
    }
}
