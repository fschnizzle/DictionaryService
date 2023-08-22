package Client;

import java.io.*;
import java.net.*;

public class DictionaryClient {

    private String hostname;
    private int port;

    /* Constructor */
    public DictionaryClient(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
    }

    /* Methods */
    public void start() {
        Socket socket = null;
        DataOutputStream socketOutput = null;
        DataInputStream socketInput = null;
        BufferedReader userInput = null;
        try {
            // Bind client to new socket on host port
            socket = new Socket(hostname, port);
            System.out.println("Connected to server on " + hostname + ":" + port);

            // Open data IO streams
            socketOutput = new DataOutputStream(socket.getOutputStream());
            socketInput = new DataInputStream(socket.getInputStream());
            userInput = new BufferedReader(new InputStreamReader(System.in));

            /* Placeholder connection testing functionality */
            while (true) {

                // Prompts user for action
                int choice = displayMenu(userInput);
                String request = handleChoice(choice, userInput);

                // Handles EXIT
                if (request.equals("EXIT")) {
                    break;
                }
                // Send Request to server
                socketOutput.writeUTF(request);
                socketOutput.flush();

                // Await response
                String response = socketInput.readUTF();
                System.out.println("Server Response: " + response);

            }

        } catch (IOException e) {
            System.out.println("Connection Refused: Check port and hostname");
        } finally {
            // Handles the graceful closure of IO streams and socket
            try {
                if (socketOutput != null) socketOutput.close();
                if (socketInput != null) socketInput.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    // Replace displayMenu with the visual GUI version later
    private int displayMenu(BufferedReader userInput) throws IOException {
        System.out.println("Choose an action:");
        System.out.println("1. QUERY");
        System.out.println("2. ADD");
        System.out.println("3. UPDATE");
        System.out.println("4. REMOVE");
        System.out.println("5. EXIT");
        System.out.print("Choice: ");
        return Integer.parseInt(userInput.readLine());
    }

    private String handleChoice(int choice, BufferedReader userInput) throws IOException{
        String command;
        String word = "";
        String meaning = "";
        switch (choice) {
            case 1: // QUERY
                command = "QUERY";
                word = promptWord("queried", userInput); // Prompts user for text input: "Which word should be queried: "
                break;
            case 2: // ADD
                command = "ADD";
                word = promptWord("added to the dictionary", userInput); // Prompts user for text input: "Which word should be added to the dictionary: "
                meaning = promptWordMeaning(word, userInput); // Prompts user for text input: "Enter definition for {word}:"
                break;
            case 3: // UPDATE
                command = "UPDATE";
                word = promptWord("updated", userInput); // Prompts user for text input: "Which word should be updated: "
                meaning = promptWordMeaning(word, userInput); // Prompts user for text input: "Enter definition for {word}:"
                break;
            case 4: // REMOVE
                command = "REMOVE";
                word = promptWord("removed from the dictionary", userInput); // Prompts user for text input: "Which word should be removed from the dictionary"
                break;
            case 5: // EXIT
                command = "EXIT";
                System.out.println("Exiting...");
                return "EXIT";
            default:
                System.out.println("Invalid choice.");
                return "";
        }

        return command + ":" + word + (meaning.isEmpty() ? "" : ":" + meaning);

    }

    private String promptWord(String action, BufferedReader userInput) throws IOException {
        System.out.println("Which word should be " + action + "?");
        return userInput.readLine();
    }

    private String promptWordMeaning(String word, BufferedReader userInput) throws IOException {
        System.out.println("Enter definition for " + word + ":");
        return userInput.readLine();
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
