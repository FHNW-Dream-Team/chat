package ch.fhnw.gitlab.bradley.richards;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.Security;
import java.util.Scanner;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;

import com.sun.net.ssl.internal.ssl.Provider;

/**
 * This is a really simple test client: It reads from the scanner, sends to the
 * server, and prints the server's response. This is all in plain-text - it is
 * up to the user to type in correctly formatted messages.
 */
public class TestClient {

    public static void main(String[] args) {
        String ipAddress = null;
        int portNumber = 0;
        boolean secure = true;

        try (Scanner in = new Scanner(System.in)) {
            boolean valid = false;

            // Read IP address
            while (!valid) {
                System.out.println("Enter the address of the server");
                ipAddress = in.nextLine();
                valid = validateIpAddress(ipAddress);
            }

            // Read port number
            valid = false;
            while (!valid) {
                System.out.println("Enter the port number on the server (1024-65535)");
                String strPort = in.nextLine();
                valid = validatePortNumber(strPort);
                if (valid) portNumber = Integer.parseInt(strPort);
            }

            // Read security
            System.out.println("Enter 'yes' if the client should use SecureSockets");
            String s = in.nextLine().trim();
            secure = s.equalsIgnoreCase("yes");

            Socket socket = null;
            try {
                if (secure) {
                    // Registering the JSSE provider
                    Security.addProvider(new Provider());

                    // Specifying the Truststore details. This is needed if you have created a
                    // truststore, for example, for self-signed certificates
                    System.setProperty("javax.net.ssl.trustStore", "truststore.ts");
                    System.setProperty("javax.net.ssl.trustStorePassword", "trustme");

                    // Creating Client Sockets
                    SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                    socket = sslsocketfactory.createSocket(ipAddress, portNumber);

                    // The next line is entirely optional !!
                    // The SSL handshake would happen automatically, the first time we send data.
                    // Or we can immediately force the handshaking with this method:
                    ((SSLSocket) socket).startHandshake();
                } else {
                    socket = new Socket(ipAddress, portNumber);
                }
                System.out.println("Connected");
                try (BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     OutputStreamWriter socketOut = new OutputStreamWriter(socket.getOutputStream())) {
                    // Create thread to read incoming messages
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                String msg;
                                try {
                                    msg = socketIn.readLine();
                                    System.out.println("Received: " + msg);
                                } catch (IOException e) {
                                    break;
                                }
                                if (msg == null) break; // In case the server closes the socket
                            }
                        }
                    };
                    Thread t = new Thread(r);
                    t.start();

                    // Loop, allowing the user to send messages to the server
                    // Note: We still have our scanner
                    System.out.println("Enter commands to server or ctrl-D to quit");
                    while (in.hasNext()) {
                        String line = in.nextLine();
                        socketOut.write(line + "\n");
                        socketOut.flush();
                        System.out.println("Sent: " + line);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (socket != null) try {
                    socket.close();
                } catch (IOException e) {
                    // we don't care
                }
            }
        }
    }

    private static boolean validateIpAddress(String ipAddress) {
        boolean formatOK = false;
        // Check for validity (not complete, but not bad)
        String ipPieces[] = ipAddress.split("\\."); // Must escape (see
        // documentation)
        // Must have 4 parts
        if (ipPieces.length == 4) {
            // Each part must be an integer 0 to 255
            formatOK = true; // set to false on the first error
            int byteValue = -1;
            for (String s : ipPieces) {
                byteValue = Integer.parseInt(s); // may throw
                // NumberFormatException
                if (byteValue < 0 | byteValue > 255) formatOK = false;
            }
        }
        return formatOK;
    }

    private static boolean validatePortNumber(String portText) {
        boolean formatOK = false;
        try {
            int portNumber = Integer.parseInt(portText);
            if (portNumber >= 1024 & portNumber <= 65535) {
                formatOK = true;
            }
        } catch (NumberFormatException e) {
        }
        return formatOK;
    }
}
