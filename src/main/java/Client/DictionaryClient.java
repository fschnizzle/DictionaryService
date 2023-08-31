/**
 * COMP90015: Distributed Systems - Assignment 1
 * File: DictionaryClient.java
 *
 * Author: Flynn Schneider
 * Student ID: 982143
 * Date: 31/8/23
 *
 * Description: This class creates a client connections and facilitates the connection
 * to an active Dictionary Server, and then requests to that server.
 * Usage: java -jar DictionaryClient.jar <server-address> <port>
 */

package Client;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DictionaryClient {
    private String hostname;
    private int port;
    private BlockingQueue<String> requestQueue = new LinkedBlockingQueue<>();
    private clientRequestForm formGUI;

    /* Constructor */
    public DictionaryClient(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
    }

    // Setters
    public void setForm(clientRequestForm formGUI) {
        this.formGUI = formGUI;
    }

    /* Methods */
    public void processRequest(String request) {
        try {
            requestQueue.put(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public void start() {
        try {
            // Bind client to new socket on host port
            final Socket socket = new Socket(hostname, port);

            // Start GUI
            setForm(new clientRequestForm(this));
            formGUI.updateESMessage(false, "Connected to server on " + hostname + ":" + port);

            // Open data IO streams
            final DataOutputStream socketOutput = new DataOutputStream(socket.getOutputStream());
            final DataInputStream socketInput = new DataInputStream(socket.getInputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        // Wait for a request to become available and take it from the queue
                        String request = requestQueue.take();

                        // Send Request to the server
                        socketOutput.writeUTF(request);
                        socketOutput.flush();

                        // Await response from the server
                        String response = socketInput.readUTF();

                        // Handle GUI Update
                        handleGUIupdate(request, response);

                    }
                } catch (Exception e) {
                    System.out.println("An error occurred: " + e.getMessage());
                    formGUI.updateESMessage(true, "An error occurred: " + e.getMessage());
                    Thread.currentThread().interrupt();
                } finally {
                    // Close resources
                    try {
                        socketOutput.close();
                        socketInput.close();
                        socket.close();
                    } catch (IOException e) {
                        System.out.println("Error closing socket resources: " + e.getMessage());
                    }
                }
            }).start();

        } catch (IOException e) {
            // Show connection error in command line.
            System.out.println("Connection Refused: Ensure host and port number are valid and that server is active.");
        }
    }
    private void handleGUIupdate(String req, String resp) {
        // Parse Command, word, response
        String[] parseReq = req.split(":");
        String command = parseReq[0];
        String word = parseReq[1];
        String statusMessage;

        // Switch case
        switch (command) {
            case "QUERY":
                // Check for successful Query prefix "/Q/"
                String prefix = resp.substring(0, 3);
                if (prefix.equals("/Q/")){
                    formGUI.updateQueryOutput(word, resp.substring(3));
                    formGUI.updateESMessage(false, "Queried: '" + word + "'");

                } else{
                    formGUI.updateESMessage(true,command + " ERROR: Word '" + word + "' not found in dictionary");
                }
                break;
            case "ADD":
                if (resp.equals("SUCCESS")) {
                    statusMessage = "Successfully added ";
                    formGUI.updateESMessage(false, " " + statusMessage + "'" + word + "'.");
                } else {
                    formGUI.updateESMessage(true, command + " ERROR: Could not add '" + word + "'. It might already exist. Try UPDATE instead.");
                }
                break;
            case "UPDATE":
                if (resp.equals("SUCCESS")) {
                    statusMessage = "Successfully updated ";
                    formGUI.updateESMessage(false, " " + statusMessage + "'" + word + "'.");
                } else {
                    formGUI.updateESMessage(true, command + " ERROR: Could not update '" + word + "'. It might not exist. Try ADD instead.");
                }
                break;
            case "REMOVE":
                if (resp.equals("SUCCESS")) {
                    statusMessage = "Successfully removed ";
                    formGUI.updateESMessage(false, " " + statusMessage + "'" + word + "'.");
                } else {
                    formGUI.updateESMessage(true, command + " ERROR: Could not remove '" + word + "'. It might not exist.");
                }
                break;
            default:
                formGUI.updateESMessage(true, "Unknown command ERROR: " + command);
        }
    }
    public static void main(String[] args){
        // Takes command line call like this: java –jar DictionaryClient.jar <server-address> <server-port>

        // Check for valid command line arguments
        if (args.length != 2) {
            System.out.println("Usage: java -jar DictionaryClient.jar <server-address> <port>");
            return;
        }

        // Parse arguments and set port, hostname (server-address), and new client
        try {
            String hostname = args[0];
            int port = Integer.parseInt(args[1]);

            DictionaryClient client = new DictionaryClient(hostname, port);
            client.start();
        } catch (NumberFormatException e) {
            System.out.println("Error: Port must be an integer.");
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

}
