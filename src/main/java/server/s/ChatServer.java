package server.s;

// Application configuration and interfaces
import common.config.AppConfig;
import common.interfaces.IDisplayService;

// Model classes
import common.model.ChatMessage;

// Service layer
import common.services.ColorGenerator;

// JavaFX for UI updates
import javafx.application.Platform;

// Java I/O and networking
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// Java collections and concurrency
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Main server class that handles client connections and message broadcasting.
 * Manages a list of connected clients and coordinates communication between them.
 */
public class ChatServer extends Thread {
    // Server state and components
    private final IDisplayService displayService;  // Service for displaying server messages
    private ServerSocket serverSocket;            // Listens for client connections
    private final List<ClientHandler> clients;    // Thread-safe list of connected clients
    private volatile boolean running;             // Controls the server's main loop

    /**
     * Creates a new chat server instance.
     *
     * @param displayService Service for displaying server messages and notifications
     * @param port The port number to listen on for client connections
     */
    public ChatServer(IDisplayService displayService, int port) {
        this.displayService = displayService;
        this.clients = new CopyOnWriteArrayList<>();  // Thread-safe list implementation
        this.running = true;  // Initialize server as running

        try {
            // Create server socket on the specified port
            serverSocket = new ServerSocket(port);
            // Display server start message
            displayMessage(ChatMessage.createTextMessage("SERVER", AppConfig.SERVER_MESSAGE_COLOR,
                    "Server started on port " + port));
        } catch (IOException e) {
            // Display error if server cannot start
            displayMessage(ChatMessage.createTextMessage("SERVER", AppConfig.ERROR_COLOR,
                    "Error: " + e.getMessage()));
        }
    }

    /**
     * Main server loop that accepts client connections.
     * Runs in a separate thread to prevent blocking the UI.
     */
    @Override
    public void run() {
        while (running) {
            try {
                // Wait for a client to connect
                Socket clientSocket = serverSocket.accept();
                // Get client information
                String clientIP = clientSocket.getInetAddress().getHostAddress();
                // Generate a random color for the client's messages
                String color = ColorGenerator.generateRandomColor();

                // Create a new handler for this client
                ClientHandler clientHandler = new ClientHandler(clientSocket, this, clientIP, color);
                clients.add(clientHandler);
                // Start the client handler thread
                clientHandler.start();
            } catch (IOException e) {
                // Only report errors if the server is still supposed to be running
                if (running) {
                    displayMessage(ChatMessage.createTextMessage("SERVER", AppConfig.ERROR_COLOR,
                            "Error accepting client: " + e.getMessage()));
                }
            }
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message The message to broadcast
     */
    public void broadcastMessage(ChatMessage message) {
        // Send the message to each connected client
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    /**
     * Removes a client from the list of connected clients.
     * Called when a client disconnects or encounters an error.
     *
     * @param client The client handler to remove
     */
    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    /**
     * Displays a message in the server's UI.
     * Handles both text and image messages appropriately.
     *
     * @param message The message to display
     */
    public void displayMessage(ChatMessage message) {
        // Ensure UI updates happen on the JavaFX Application Thread
        Platform.runLater(() -> {
            if (message.isImageMessage()) {
                // Handle image messages
                try {
                    // Decode the image data into a temporary file
                    java.io.File imageFile = common.services.ImageService.decodeImage(
                            message.getImageData(), message.getFileName());
                    // Format the message with sender information
                    String label = common.services.MessageFormatter.formatForDisplay(message);
                    // Display the image with the formatted label
                    displayService.displayImage(label, imageFile.getAbsolutePath(),
                            message.getColor() != null ? message.getColor() : AppConfig.SERVER_MESSAGE_COLOR);
                } catch (Exception e) {
                    // Show error if image cannot be loaded
                    displayService.displayText(
                            "[" + message.getSender() + "] Failed to load image: " + message.getFileName(),
                            AppConfig.ERROR_COLOR);
                }
            } else {
                // Handle text messages
                String displayText = common.services.MessageFormatter.formatForDisplay(message);
                // Display the text with appropriate color
                displayService.displayText(displayText,
                        message.getColor() != null ? message.getColor() : AppConfig.SERVER_MESSAGE_COLOR);
            }
        });
    }

    /**
     * Gracefully shuts down the server.
     * Stops accepting new connections and closes all existing client connections.
     */
    public void shutdown() {
        running = false;  // Signal the server loop to stop
        try {
            // Interrupt all client handler threads
            for (ClientHandler client : clients) {
                client.interrupt();
            }
            clients.clear();  // Clear the list of clients
            
            // Close the server socket if it's open
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            // Log any errors during shutdown
            System.err.println("Error shutting down server: " + e.getMessage());
        }
    }
}
