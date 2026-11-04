import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.SimpleDateFormat;

/**
 * The Server class manages the online sign-up system.
 * It handles multiple client connections using a thread pool
 * and maintains the state of the sign-up lists.
 */
public class Server {

    // 1. State variables with default values
    private int numberOfLists = 0;
    private int maxMembersPerList = 0;
    private int portNumber = 8888;
    private ExecutorService threadPool = null;
    private List<List<String>> signLists = new ArrayList<>();
    private File logFile = new File("log.txt");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 2. Constructor
    /**
     * Constructor to initialize the server with list constraints.
     * It also sets up the thread pool and initializes the log file.
     * * @param numLists   The total number of lists available.
     * @param maxMembers The maximum number of members allowed in each list.
     */
    public Server(int numLists, int maxMembers) {
        this.numberOfLists = numLists;
        this.maxMembersPerList = maxMembers;
        this.threadPool = Executors.newFixedThreadPool(20);

        // Initialize empty lists
        for (int i = 0; i < this.numberOfLists; i++) {
            this.signLists.add(new ArrayList<>());
        }

        // Initialize log file (delete if exists, then create new)
        if (this.logFile.exists()) {
            this.logFile.delete();
        }
        try {
            this.logFile.createNewFile();
        } catch (IOException e) {
            System.err.println("Warning: Could not create log file.");
        }
    }

    // 3. Core Methods
    /**
     * Starts the server to listen for incoming connections on port 8888.
     * Assigns each incoming connection to a new thread in the thread pool.
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(this.portNumber)) {
            System.out.println("Server started successfully on port " + this.portNumber);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                this.threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
        }
    }

    /**
     * Synchronized method to log client requests safely from multiple threads.
     * * @param ipAddress The IP address of the client.
     * @param request   The command sent by the client.
     */
    private synchronized void logRequest(String ipAddress, String request) {
        String timestamp = this.dateFormat.format(new Date());
        String logEntry = timestamp + " " + ipAddress + "|" + request;
        try (PrintWriter fw = new PrintWriter(new FileWriter(this.logFile, true))) {
            fw.println(logEntry);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    /**
     * Generates a summary of all lists and their member counts.
     * * @return Formatted string containing the totals.
     */
    private synchronized String handleTotals() {
        StringBuilder sb = new StringBuilder();
        sb.append("There are ").append(this.numberOfLists)
          .append(" list(s), each with a maximum size of ").append(this.maxMembersPerList).append(".\n");
        
        for (int i = 0; i < this.numberOfLists; i++) {
            sb.append("List ").append(i + 1).append(" has ")
              .append(this.signLists.get(i).size()).append(" member(s).");
            if (i < this.numberOfLists - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Retrieves all members of a specified list.
     * * @param listIndex The 1-based index of the list.
     * @return String of members separated by newlines, or an error message.
     */
    private synchronized String handleList(int listIndex) {
        if (listIndex < 1 || listIndex > this.numberOfLists) {
            return "Error: Invalid list number.";
        }
        List<String> targetList = this.signLists.get(listIndex - 1);
        if (targetList.isEmpty()) {
            return ""; // No members to show
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < targetList.size(); i++) {
            sb.append(targetList.get(i));
            if (i < targetList.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Attempts to add a new member to a specified list.
     * * @param listIndex The 1-based index of the list.
     * @param name      The name of the member to add.
     * @return "Success." if added, "Failed." otherwise.
     */
    private synchronized String handleJoin(int listIndex, String name) {
        if (listIndex < 1 || listIndex > this.numberOfLists) {
            return "Failed.";
        }
        List<String> targetList = this.signLists.get(listIndex - 1);
        if (targetList.size() >= this.maxMembersPerList) {
            return "Failed.";
        }
        targetList.add(name);
        return "Success.";
    }

    /**
     * Inner class to handle individual client connections concurrently.
     */
    private class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(this.clientSocket.getOutputStream(), true)
            ) {
                String request = in.readLine();
                if (request == null || request.trim().isEmpty()) {
                    return;
                }

                String ipAddress = this.clientSocket.getInetAddress().getHostAddress();
                
                // Split the request into max 3 parts: [command] [listNumber] [name (with spaces)]
                String[] parts = request.split(" ", 3);
                String command = parts[0].toLowerCase();

                boolean isValidRequest = false;
                String response = "Error: Unknown or invalid command.";

                // Parse commands
                if (command.equals("totals")) {
                    response = handleTotals();
                    isValidRequest = true;
                } else if (command.equals("list") && parts.length >= 2) {
                    try {
                        int listNum = Integer.parseInt(parts[1]);
                        response = handleList(listNum);
                        isValidRequest = true;
                    } catch (NumberFormatException e) {
                        response = "Error: List number must be an integer.";
                    }
                } else if (command.equals("join") && parts.length >= 3) {
                    try {
                        int listNum = Integer.parseInt(parts[1]);
                        String name = parts[2];
                        response = handleJoin(listNum, name);
                        isValidRequest = true;
                    } catch (NumberFormatException e) {
                        response = "Failed.";
                    }
                }

                // Log request if it matches expected patterns
                if (isValidRequest) {
                    logRequest(ipAddress, request);
                }

                // Send response back to client
                out.println(response);

            } catch (IOException e) {
                // Ignore silent disconnects
            } finally {
                try { 
                    this.clientSocket.close(); 
                } catch (IOException e) { 
                    // Ignore close exceptions
                }
            }
        }
    }

    public static void main(String[] args) {
        // Immediate check for minimum arguments required
        if (args.length < 2) {
            System.err.println("Error: Please provide number of lists and max members per list.");
            System.exit(1);
        }

        try {
            int numLists = Integer.parseInt(args[0]);
            int maxMembers = Integer.parseInt(args[1]);
            
            Server myServer = new Server(numLists, maxMembers);
            myServer.start();
        } catch (NumberFormatException e) {
            System.err.println("Error: Arguments must be valid integers.");
            System.exit(1);
        }
    }
}