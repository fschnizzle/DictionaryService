package Server;
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;
import java.nio.file.*;
import java.util.Map;
public class DictionaryServer {

    private int port;
    private ServerSocket serverSocket;
    private ConcurrentHashMap dictionary;

    // Instance of DictionaryDataHandler to handle file operations
    private DictionaryDataHandler dataHandler = new DictionaryDataHandler();
    private String jsonFilePath;


    /* Setters */
    public DictionaryServer(int port, String jsonFilePath) {
        this.port = port;
        setJsonFilePath(jsonFilePath);
    }

    public void setDictionary(String jsonFilePath) throws IOException {
        this.dictionary = dataHandler.loadDictionaryFromFile(jsonFilePath);
    }

    public void setJsonFilePath(String filePath){
        this.jsonFilePath = filePath;
    }

    /* Methods */
    public boolean wordExists(String word){
        return dictionary.containsKey(word);
    }

    public String getDefinitions(String word){
        String definitions = (String) this.dictionary.get(word);
        definitions = definitions.replace(';','\n');
        return definitions;
    }
    public void start() {
        // Create a thread pool with a fixed number of threads (e.g., 10)
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        // Initialise ServerSocket and bind to port
        try {
            // Load initial data from JSON file into ConcurrentHashMap
            setDictionary(this.jsonFilePath);
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
                        // String definitions = getDefinitions(word);
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
                        //response = word + " successfully added to dictionary";
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
//                        //response = word + " successfully updated in dictionary";
                    }
                    // Return {word does NOT exist}: 'Not in dictionary' msg
                    else {
                        response = "'" + word + "' is not in the dictionary. Use ADD instead.";
                    }
                    // response = word + " updated with new definition: " + meaning;
                    break;
                case "REMOVE":
                    // Return {word exists}: (Remove word.) Inform user of removal success
                    if (wordExists(word)){
                        // Remove word in dictionary
                        dictionary.remove(word);
                        response = "SUCCESS";
//                        //response = word + " successfully removed from dictionary";
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
        } catch (Exception e) {
            // TODO: Handle error for setting port, Dictfilepath or server
            e.printStackTrace();
        }
    }
}
