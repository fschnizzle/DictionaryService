package Client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class DictionaryClient {

    private String hostname;
    private int port;
    private BlockingQueue<String> requestQueue = new LinkedBlockingQueue<>();


    private clientRequestForm formGUI;

    /* FOR NOW */
//    private JFrame frame;
//    private JButton queryButton, addButton, updateButton, removeButton, exitButton, helpButton;


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
            System.out.println("BEFORE");
            setForm(new clientRequestForm(this));
            System.out.println("AFTER");

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
                    formGUI.updateESMessage(false,"");
                } else{
                    formGUI.updateESMessage(true,"Error: Word '" + word + "' not found in dictionary");
                }
                break;
            case "ADD":
                if (resp.equals("SUCCESS")) {
                    statusMessage = "Successfully added " + word;
                    formGUI.updateESMessage(false, "");
                } else {
                    formGUI.updateESMessage(true, "Error: Could not add '" + word + "'. It might already exist.");
                }
                break;
            case "UPDATE":
                if (resp.equals("SUCCESS")) {
                    statusMessage = "Successfully updated " + word;
                    formGUI.updateESMessage(false, "");
                } else {
                    formGUI.updateESMessage(true, "Error: Could not update '" + word + "'. It might not exist.");
                }
                break;
            case "REMOVE":
                if (resp.equals("SUCCESS")) {
                    statusMessage = "Successfully removed " + word;
                    formGUI.updateESMessage(false, "");
                } else {
                    formGUI.updateESMessage(true, "Error: Could not remove '" + word + "'. It might not exist.");
                }
                break;
            default:
                formGUI.updateESMessage(true, "Unknown command: " + command);
        }
    }

    // ... other methods like getValidRequest, validateWord, etc.



//    private void createGUI() {
//        // Sample GUI (not linked to anything)
//        // Should be contained within its own class
//
//        frame = new JFrame("Dictionary Client");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(300, 200);
//
//        queryButton = new JButton("QUERY");
//        addButton = new JButton("ADD");
//        updateButton = new JButton("UPDATE");
//        removeButton = new JButton("REMOVE");
//        exitButton = new JButton("EXIT");
//        helpButton = new JButton("HELP");
//
//        frame.setLayout(new FlowLayout());
//
//        frame.add(queryButton);
//        frame.add(addButton);
//        frame.add(updateButton);
//        frame.add(removeButton);
//        frame.add(exitButton);
//        frame.add(helpButton);
//
//        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
//
//        queryButton.addActionListener(e -> {
//            try {
//                handleChoice(1, userInput);
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
//        });
////        addButton.addActionListener(e -> handleChoice(2));
////        updateButton.addActionListener(e -> handleChoice(3));
////        removeButton.addActionListener(e -> handleChoice(4));
////        exitButton.addActionListener(e -> handleChoice(5));
////        helpButton.addActionListener(e -> handleChoice(6));
//
//        frame.setVisible(true);
//    }

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
