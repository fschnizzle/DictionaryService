package Client;

import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        int choice;
        String request = "";
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
                // Displays options (with detailed help menu) if request is for "HELP"
//                if (!request.equals("HELP")){
//                    // Prompts user for action
//                    choice = displayMenu(userInput);
//                    request = handleChoice(choice, userInput);
//                }
//                while (request.equals("HELP")){
//                    // Prompts user for action with detailed choices
//                    choice = displayHelpMenu(userInput);
//                    request = handleChoice(choice, userInput);
//                }
                request = getValidRequest(userInput, request);


                // Send Request to server
                socketOutput.writeUTF(request);
                socketOutput.flush();

                // Await response
                String response = socketInput.readUTF();
                System.out.println("Server Response: " + response);

                // Handles EXIT
                if (request.equals("EXIT")) {
                    break; // Finally block handles IO / socket closure
                }

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

    private String getValidRequest(BufferedReader userInput, String request) throws IOException {
        int choice = -1;
        do { // Continue until a valid integer is entered
            try { // Catch non integer inputs
                do { // Catch input values outside of range 1-6
                    choice = displayMenu(userInput);
                    request = handleChoice(choice, userInput);
                    while (request.equals("HELP")) {
                        choice = displayHelpMenu(userInput);
                        request = handleChoice(choice, userInput);
                    }
                } while (request.equals("RECHOOSE"));


            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
                choice = -1;
            }
        } while (choice == -1);
        return request;
    }

    // Replace displayMenu with the visual GUI version later
    private int displayMenu(BufferedReader userInput) throws IOException {
        System.out.println("Choose an action:");
        System.out.println("1. QUERY");
        System.out.println("2. ADD");
        System.out.println("3. UPDATE");
        System.out.println("4. REMOVE");
        System.out.println("5. EXIT");
        System.out.println("6. HELP");
        System.out.print("Choice: ");
        return Integer.parseInt(userInput.readLine());
    }
    // Replace displayMenu with the visual GUI version later
    private int displayHelpMenu(BufferedReader userInput) throws IOException {
        System.out.println("Help menu:");
        System.out.println("1: QUERY - Query a word in the dictionary. [Fails if word NOT IN dictionary]");
        System.out.println("2: ADD - Add a word and its meaning to the dictionary. [Fails if word ALREADY IN dictionary]");
        System.out.println("3: UPDATE - Update the meaning of a word in the dictionary. [Fails if word NOT IN dictionary]");
        System.out.println("4: REMOVE - Remove a word from the dictionary. [Fails if word NOT IN dictionary]");
        System.out.println("5: EXIT - Exit the application.");
        System.out.println("6: HELP - Show this help menu.");
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
            case 6: // HELP
                command = "HELP";
                return "HELP";
            default:
                System.out.println("Invalid choice. Choose from the options listed below.");
                return "RECHOOSE";
        }

        return command + ":" + word + (meaning.isEmpty() ? "" : ":" + meaning);

    }

    public static boolean isValidWord(String word) {
        // Checks if the word contains only alphabetical characters
        Pattern pattern = Pattern.compile("^[a-zA-Z]+$");
        Matcher matcher = pattern.matcher(word);
        return matcher.matches();
    }
    private String promptWord(String action, BufferedReader userInput) throws IOException {
        String word;
        do {
            System.out.println("Which word should be " + action + "?");
            word = userInput.readLine();
            if (!isValidWord(word)) {
                System.out.println("Invalid input. Please enter a word made up of only alphabetical characters.");
            }
        } while (!isValidWord(word));
        return word;
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
