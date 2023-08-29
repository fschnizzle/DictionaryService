package Client;

import javax.swing.*;

public class clientRequestForm extends JFrame {

    private DictionaryClient client;
    private  JTextArea responseField; /* Delete Later: "To display server responses" */

    /* Initialise JFrame Component */
    private JTextArea ErrorSuccessTextArea;
    private JPanel SuccessErrorMessage;
    private JPanel HeaderPanel;
    private JPanel WordMeaningFields;
    private JPanel WordMeaningForm;
    private JTextField WordInputField;
    private JPanel WordField;
    private JLabel WordInputLabel;
    private JTextField MeaningInputField;
    private JLabel MeaningInputLabel;
    private JPanel MeaningField;
    private JButton AddButton;
    private JButton UpdateButton;
    private JButton DeleteButton;
    private JPanel FormButtons;
    private JPanel DisplayQueryBox;
    private JTextArea WordHeading;
    private JTextArea MeaningOutput;
    private JPanel DisplayWordMeaning;
    private JPanel QueryForm;
    private JTextField QueryInputField;
    private JButton QueryButton;
    private JPanel MainPanel;
    private JLabel WordToQueryLabel;
    private JLabel HeaderLabel;

    /* Constructor */
    public clientRequestForm(DictionaryClient client){
        this.client = client;
        createAndShowGUI();
    }

    /* METHODS */
    private void handleChoice(int choice){
        String request = "";
        String command = "";
        String word = "";
        String meaning = "";
        boolean isValid = false;
        String ESMessage = "";
        System.out.println("and here");

        switch (choice) {
            case 1: // QUERY
                command = "QUERY";
                word = QueryInputField.getText().trim().toLowerCase(); // Gets input from the WordInputField JTextField
                isValid = validateWord(word);
                break;
            case 2: // ADD
                command = "ADD";
                word = WordInputField.getText().trim().toLowerCase(); // Gets input from the WordInputField JTextField
                meaning = MeaningInputField.getText().trim(); // Gets input from the MeaningInputField JTextField
                isValid = validateWord(word) && validateMeaning(meaning);
                System.out.println(("ATTEMPTING TO ADD: " + word + " with meaning: " + meaning));
                break;
            case 3: // UPDATE
                command = "UPDATE";
                word = WordInputField.getText().trim().toLowerCase(); // Gets input from the WordInputField JTextField
                meaning = MeaningInputField.getText().trim(); // Gets input from the MeaningInputField JTextField
                isValid = validateWord(word) && validateMeaning(meaning);
                break;
            case 4: // REMOVE
                command = "REMOVE";
                word = WordInputField.getText().trim().toLowerCase(); // Gets input from the WordInputField JTextField
                isValid = validateWord(word);
                break;
            case 5: // EXIT
                command = "EXIT";
                System.out.println("Exiting...");
                request = "EXIT";
            case 6: // HELP
                command = "HELP";
                request = "HELP";
            default:
                System.out.println("Invalid choice. Choose from the options listed below.");
                request = "RECHOOSE";
        }

        // Check input validity
        if (isValid) {
//            System.out.println(("ATTEMPTING TO" + command + " " + word));
            request = command + ":" + word + (meaning.isEmpty() ? "" : ":" + meaning);
            client.processRequest(request);
        } else {
            ESMessage = "ERROR: Invalid word or meaning given.";
//            System.out.println("Invalid word or meaning given.");
            this.updateErrorSuccessOutput(ESMessage);
//            System.out.println("Invalid word or meaning entered. Use alphabet characters only for word and ____.");
        }

//
//        if(!isValid) {
//            request = "INVALID";
//        }



        // For update, add returns format --> COMMAND:WORD:MEANING
        // For remove, query returns format --> COMMAND:WORD
        // Return command + ":" + word + (meaning.isEmpty() ? "" : ":" + meaning);
        // client.processRequest(command + ":" + word + (meaning.isEmpty() ? "" : ":" + meaning));
    }

    private boolean validateWord(String word) {
        // Checks for valid word regex pattern
        return word.matches("^[a-zA-Z]+$");
    }
    private boolean validateMeaning(String meaning) {
        // Checks to ensure it isn't empty. Doesn't check for anything else.
        return !meaning.isEmpty();
    }

    public void updateErrorSuccessOutput(String serverResponse){
        //clientRequestForm.
        // Update the GUI based on the server's response
        // For example, if you have a JTextArea to display the server's response:
        ErrorSuccessTextArea.setText(serverResponse);
    }

    public void updateGUI(){
        //clientRequestForm.
        // Update the GUI based on the server's response
        // For example, if you have a JTextArea to display the server's response:
//        ErrorSuccessTextArea.setText("Error: Invalid Input");
    }

    public void updateQueryOutput(String word, String resMeaning){
        // Update Query Output with Word and server response meaning
        String capWord = word.substring(0, 1).toUpperCase() + word.substring(1);
        WordHeading.setText(capWord);

        // Get meanings and format as numbered
        String[] meanings = resMeaning.split("\n");
        StringBuilder numberedMeanings = new StringBuilder();

        for (int i = 0; i < meanings.length; i++) {
            numberedMeanings.append((i + 1) + ". " + meanings[i]);
            if (i < meanings.length - 1) { // avoid adding a newline at the end
                numberedMeanings.append("\n");
            }
        }

        MeaningOutput.setText(numberedMeanings.toString());
    }

    public void updateESMessage(Boolean isError, String errorSuccessMessage){
        // Update the Error / Success Text field with message
        ErrorSuccessTextArea.setText(errorSuccessMessage);

        // if isError then update icon to a RED CROSS
        // else update icon to a GREEN TICK
    }
    public void createAndShowGUI(){
        setContentPane(MainPanel);
        setTitle("Simple GUI App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setVisible(true);

        /* Attach event listeners to buttons*/
        QueryButton.addActionListener(e -> handleChoice(1));
        // Rest of event listeners
        AddButton.addActionListener(e -> handleChoice(2));
//        System.out.println("there");
        UpdateButton.addActionListener(e -> handleChoice(3));
        DeleteButton.addActionListener(e -> handleChoice(4));
//        exitButton.addActionListener(e -> handleChoice(5));
//        helpButton.addActionListener(e -> handleChoice(6));





    }


    // DELETE LATER: FOR TESTINg
//    public static void main(String[] args){
//        // HARDCODED
//        new clientRequestForm(new DictionaryClient("localhost", 9999));
//    }
}
