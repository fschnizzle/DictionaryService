package Server;
import java.io.*;
import java.net.*;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
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
        // Initialise ServerSocket and bind to port
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            // Continuously listen for incoming clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Handle the client connection in a separate thread
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        // TODO: Handle the client's request.
        // Read the command, delegate to CommandHandler, and respond to the client.
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
