package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;

public class clientRequestForm extends JFrame {

    private DictionaryClient client; // Connects to a single client

    /* Initialise JFrame Components */
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
            default:
                break;
        }

        // Check input validity
        if (isValid) {
            request = command + ":" + word + (meaning.isEmpty() ? "" : ":" + meaning);
            client.processRequest(request);
        } else {
            ESMessage = command + " ERROR: Invalid word or meaning given.";
            this.updateESMessage(true, ESMessage);
        }
    }

    private boolean validateWord(String word) {
        // Checks for valid word regex pattern
        return word.matches("^[a-zA-Z]+$");
    }
    private boolean validateMeaning(String meaning) {
        // Checks to ensure it isn't empty. Doesn't check for anything else.
        return !meaning.isEmpty();
    }
    public void updateQueryOutput(String word, String resMeaning){
        // Updates Query Output with Word and server response meaning
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
        Color darkishGreen = new Color(0, 128, 0); // RGB values for dark green

        // Update the Error / Success Text field with coloured message
        if (isError){
            // Change font colour to RED for ERROR messages
            ErrorSuccessTextArea.setForeground(Color.RED);
        }
        else{
            // Change font colour to DARK GREEN for SUCCESS messages
            ErrorSuccessTextArea.setForeground(darkishGreen);
        }
        ErrorSuccessTextArea.setText(errorSuccessMessage);
    }
    public void createAndShowGUI(){
        setContentPane(MainPanel);
        setTitle("Simple GUI App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setVisible(true);

        /* Event Listeners */
        QueryButton.addActionListener(e -> handleChoice(1));
        AddButton.addActionListener(e -> handleChoice(2));
        UpdateButton.addActionListener(e -> handleChoice(3));
        DeleteButton.addActionListener(e -> handleChoice(4));
    }
}
