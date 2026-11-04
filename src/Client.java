import java.io.*;
import java.net.*;

/**
 * The Client class sends user commands to the sign-up system server.
 * It interacts over a TCP socket connection and prints the server's response.
 */
public class Client {

    // 1. State variables with default values
    private String serverAddress = "localhost";
    private int serverPort = 8888;

    // 2. Constructor
    /**
     * Default constructor for the client.
     */
    public Client() {
        // Keeps default serverAddress and serverPort
    }

    // 3. Core Methods
    /**
     * Connects to the server, sends the provided command arguments, 
     * and prints the response.
     * * @param args The command line arguments passed to the application.
     */
    public void executeCommand(String[] args) {
        // Check for empty input
        if (args.length < 1) {
            System.err.println("Error: Please provide a command (e.g., totals, list n, join n name).");
            System.exit(1);
        }

        // Join arguments into a single string.
        // Example: ["join", "1", "William Haydon"] becomes "join 1 William Haydon"
        String commandString = String.join(" ", args);

        try (
            Socket socket = new Socket(this.serverAddress, this.serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            // Send the command to the server
            out.println(commandString);

            // Read the server's response line by line and print to console
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                // If server returns empty string (e.g. for empty lists), it prints a blank line
                System.out.println(responseLine); 
            }

        } catch (ConnectException e) {
            System.err.println("Client exception: Could not connect to the server on " + this.serverAddress + ":" + this.serverPort);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Client IO exception: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        Client myClient = new Client();
        myClient.executeCommand(args);
    }
}