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
//        Socket socket = null;
//        DataOutputStream socketOutput = null;
//        DataInputStream socketInput = null;
        BufferedReader userInput = null;
        try {
            // Bind client to new socket on host port
            final Socket socket = new Socket(hostname, port);
            System.out.println("Connected to server on " + hostname + ":" + port);

            // Start GUI
            setForm(new clientRequestForm(this));

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

                        // Handle EXIT case
                        // TODO: Delete later
                        if ("EXIT".equals(request)) {
                            break;
                        }

                        // Handle GUI Update
                        handleGUIupdate(request, response);

                    }
                } catch (InterruptedException | IOException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    // Close resources
                    try {
                        if (socketOutput != null) socketOutput.close();
                        if (socketInput != null) socketInput.close();
                        if (socket != null) socket.close();
                    } catch (IOException e) {
                        System.out.println("Error closing resources: " + e.getMessage());
                    }
                }
            }).start();

        } catch (IOException e) {
            System.out.println("Connection Refused: Check port and hostname");
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
                    formGUI.updateESMessage(false, command + " SUCCESS!");

                } else{
                    formGUI.updateESMessage(true,command + " ERROR: Word '" + word + "' not found in dictionary");
                }
                break;
            case "ADD":
                if (resp.equals("SUCCESS")) {
                    statusMessage = "Successfully added " + word;
                    formGUI.updateESMessage(false, command + " " + resp + "!");
                } else {
                    formGUI.updateESMessage(true, command + " ERROR: Could not add '" + word + "'. It might already exist. Try UPDATE instead.");
                }
                break;
            case "UPDATE":
                if (resp.equals("SUCCESS")) {
                    statusMessage = "Successfully updated " + word;
                    formGUI.updateESMessage(false, command + " " + resp + "!");
                } else {
                    formGUI.updateESMessage(true, command + " ERROR: Could not update '" + word + "'. It might not exist. Try ADD instead.");
                }
                break;
            case "REMOVE":
                if (resp.equals("SUCCESS")) {
                    statusMessage = "Successfully removed " + word;
                    formGUI.updateESMessage(false, command + " " + resp + "!");
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
        } catch (Exception e){
            // TODO: Handle error for setting port, address or client
        }
    }

}
