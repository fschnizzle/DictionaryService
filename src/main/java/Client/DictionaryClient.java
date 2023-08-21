package Client;

import java.io.*;
import java.net.*;

public class DictionaryClient {

    private String hostname;
    private int port;

    /* Constrcutor */
    public DictionaryClient(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
    }

    /* Methods */
    public void start(){
        try {
            Socket socket = new Socket(hostname, port);
            System.out.println("Connected to server on " + hostname + ":" + port);

            /* Placeholder connection testing functionality */
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write("Hello, DictionaryServer!");
            writer.newLine();
            writer.flush();

            System.out.println("Message sent to server.");

            writer.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
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
