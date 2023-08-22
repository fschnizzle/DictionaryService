package Server;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class DictionaryServer {

//    protected DictionaryService dictionaryService;
    private int port;
    private ServerSocket serverSocket;

    /* Setters */
    public DictionaryServer(int port) {
        this.port = port;
    }
//    public void setDictionaryService(DictionaryService dictionaryService) {
//        this.dictionaryService = dictionaryService;
//    }

    /* Methods */
    public void start() {
        // Create a thread pool with a fixed number of threads (e.g., 10)
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        // Initialise ServerSocket and bind to port
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            // Continuously listen for incoming clients
            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Submit the client handling task to the thread pool
                threadPool.submit(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        // Handle disconnection from client
                        // System.out.println("Connection with client lost.");
                    }
                });

                // Handle the client connection in a separate thread
                // handleClient() essentially overrides the run() function of runnable interface
//                new Thread(() -> {
//                    try {
//                        handleClient(clientSocket);
//                    } catch (IOException e) {
//                        //System.out.println("Connection with client lost.");
//                        // Do nothing
//                    }
//                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Shuts down threadpool
            threadPool.shutdown();
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {
        // TODO: Handle the client's request.
        // I/O streams for client messaging
        DataInputStream clientInput = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream clientOutput = new DataOutputStream(clientSocket.getOutputStream());

        // Loop until
        while (true) {

            // Read client's request
            String clientRequest = clientInput.readUTF();

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
            // [PLACEHOLDER] Handle different commands
            switch (command) {
                case "QUERY":
                    // TODO: Implement logic to query the word in the dictionary
                    response = "Definition of " + word + ": " + "[PLACEHOLDER DEFINITION]";
                    break;
                case "ADD":
                    // TODO: Implement logic to add the word and its meaning to the dictionary
                    response = word + " added with definition: " + meaning;
                    break;
                case "UPDATE":
                    // TODO: Implement logic to update the word's definition in the dictionary
                    response = word + " updated with new definition: " + meaning;
                    break;
                case "REMOVE":
                    // TODO: Implement logic to remove the word from the dictionary
                    response = word + " removed from the dictionary";
                    break;
                default:
                    response = "Invalid command";
                    break;
            }

            // Send the response back to the client
            clientOutput.writeUTF(response);
            clientOutput.flush();
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
            String JSONFilePath = args[1];

            DictionaryServer server = new DictionaryServer(port);
            server.start(); // Start server
        } catch (Exception e) {
            // TODO: Handle error for setting port, Dictfilepath or server
            e.printStackTrace();
        }


    }


}
