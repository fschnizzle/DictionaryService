/**
 * COMP90015: Distributed Systems - Assignment 1
 * File: DictionaryServer.java
 *
 * Author: Flynn Schneider
 * Student ID: 982143
 * Date: 31/8/23
 *
 * Description: This class initializes the server socket and manages client connections.
 * Usage: java -jar DictionaryServer.jar <port> <dictionary-file>
 */
// TODO: Finish declaration. Rename file DictionaryClient.jar

package Server;
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class DictionaryServer {

    private int port;
    private ServerSocket serverSocket;
    private ConcurrentHashMap dictionary;

    // Instance of DictionaryDataHandler to handle dictionary data file operations
    private DictionaryDataHandler dataHandler = new DictionaryDataHandler();
    private String jsonFilePath;


    /* Constructor */
    public DictionaryServer(int port, String jsonFilePath) {
        this.port = port;
        setJsonFilePath(jsonFilePath);
    }

    /* Setters */
    public void setDictionary(String jsonFilePath) throws IOException {
        this.dictionary = dataHandler.loadDictionaryFromFile(jsonFilePath);
    }

    public void setJsonFilePath(String filePath){
        this.jsonFilePath = filePath;
    }

    /* Methods */
    public boolean wordExists(String word){
        // Checks if the word in the 'dictionary' hash map structure
        return dictionary.containsKey(word);
    }

    public String getDefinitions(String word){
        // Gets the meaning(s) / definition(s) of a word. Seperating meanings by ";".
        String definitions = (String) this.dictionary.get(word);
        return definitions;
    }
    public void start() {
        // Create a thread pool with a fixed number of threads
        final int numThreads = 10;
        ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);

        // Load initial data from JSON file into ConcurrentHashMap
        try {
            setDictionary(this.jsonFilePath);
        } catch (IOException e) {
            System.out.println(("ERROR: Couldn't access dictionary file. Likely an invalid file path."));
            return;
        }

        // Create a ScheduledExecutorService to save dictionary at regular intervals
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable saveTask = () -> {
            try {
                dataHandler.saveDictionaryToFile(dictionary, jsonFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        scheduler.scheduleAtFixedRate(saveTask, 0, 30, TimeUnit.SECONDS); // saves every 30 seconds

        // Initialise ServerSocket and bind to port
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            // Continuously listen for incoming clients
            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Add the client handling task to the thread pool
                threadPool.submit(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        // Handle disconnection from client
                        // Does nothing...
                    }
                });
            }
        } catch (BindException e) {
            System.out.println("ERROR: Port " + port + " is already in use. Please use a different port.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.out.println("ERROR: Could not close server socket.");
            }

            // Shuts down threadpool
            threadPool.shutdown();
            scheduler.shutdown();
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {
        // I/O streams for client messaging
        DataInputStream clientInput = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream clientOutput = new DataOutputStream(clientSocket.getOutputStream());

        // Loop until
        while (true) {
            // Read client's request
            String clientRequest = clientInput.readUTF();

            // Handle Exit argument
            if ("EXIT".equalsIgnoreCase(clientRequest)) {
                clientOutput.writeUTF("Disconnected from server");
                clientOutput.flush();
                break;
            }

            // Parse the request
            String[] requestParts = clientRequest.split(":");
            String command = requestParts[0];
            String word = requestParts.length > 1 ? requestParts[1] : "";
            String meaning = requestParts.length > 2 ? requestParts[2] : "";
            String response = "";

            // Read the command, delegate to CommandHandler, and respond to the client.
            switch (command) {
                case "QUERY":
                    // Return {word exists}: Strings for meanings on separate lines
                    if (wordExists(word)) {
                        response = "/Q/" + getDefinitions(word);
                    }
                    // Return {word NOT exist}: 'Not in dictionary' msg
                    else {
                        response = "'" + word + "' is not in the dictionary";
                    }
                    break;
                case "ADD":
                    // Return {word does not exist}: (Add word.) Inform user of update.
                    if (!wordExists(word)) {
                        // Add word to dictionary
                        dictionary.put(word, meaning);
                        response = "SUCCESS";
                    }
                    // Return {word already exists}: 'Already in dictionary' msg
                    else {
                        response = "'" + word + "' is already in the dictionary. Use UPDATE to modify '" + word  + "' meaning(s).";
                    }
                    break;
                case "UPDATE":
                    // Return {word exists}: (Update word.) Inform user of update success.
                    if (wordExists(word)) {
                        // Replace word in dictionary
                        dictionary.replace(word, meaning);
                        response = "SUCCESS";
                    }
                    // Return {word does NOT exist}: 'Not in dictionary' msg
                    else {
                        response = "'" + word + "' is not in the dictionary. Use ADD instead.";
                    }
                    break;
                case "REMOVE":
                    // Return {word exists}: (Remove word.) Inform user of removal success
                    if (wordExists(word)){
                        // Remove word in dictionary
                        dictionary.remove(word);
                        response = "SUCCESS";
                    }
                    // Return {word does NOT exist}: 'Not in dictionary' msg
                    else {
                        response = "'" + word + "' is not in the dictionary. It may have already been removed.";
                    }
                    break;
                default:
                    response = "Invalid command";
                    break;
            }

            // Send the response back to the client
            clientOutput.writeUTF(response);
            clientOutput.flush();
        }

        try {
            if (clientOutput != null) clientOutput.close();
            if (clientInput != null) clientInput.close();
        } catch (IOException e) {
            System.out.println("Error closing resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException{
        // Initialise Server Port and Server Socket
        if (args.length != 2) {
            System.out.println("Usage: java -jar DictionaryServer.jar <port> <dictionary-file>");
            return;
        }

        // Parse command line arguments for port number and JSON file
        try {
            int port = Integer.parseInt(args[0]);

            DictionaryServer server = new DictionaryServer(port, args[1]);
            server.start(); // Start server
        } catch (NumberFormatException e) {
            System.out.println("Error: Port must be an integer.");
        } catch (Exception e) {
            // Catch-all for other exceptions
            System.out.println("An unexpected error occurred.");
            e.printStackTrace();
        }
    }
}
