package server.s;

// Application configuration
import common.config.AppConfig;

// Model classes
import common.model.ChatMessage;

// Service layer
import common.services.ConnectionManager;
import common.services.MessageParser;
import common.services.MessageFormatter;

// Java I/O and networking
import java.io.IOException;
import java.net.Socket;

/**
 * Handles communication with a single client connected to the chat server.
 * Manages message receiving, sending, and client state.
 */
public class ClientHandler extends Thread {
    // Connection management
    private ConnectionManager connectionManager;  // Handles low-level network communication
    private final ChatServer server;             // Reference to the main server
    
    // Client information
    private final String clientIP;               // Client's IP address
    private final String color;                  // Color for the client's messages
    private String displayName;                  // Client's display name (initially same as IP)
    
    // State tracking
    public boolean clientInitiatedDisconnect = false;  // True if client initiated disconnect
    private boolean announced = false;                 // Whether client has announced itself

    /**
     * Creates a new client handler for the specified client connection.
     *
     * @param socket The client's socket connection
     * @param server Reference to the main chat server
     * @param clientIP The client's IP address
     * @param color The color to use for the client's messages
     * @throws IOException If an I/O error occurs when creating the connection manager
     */
    public ClientHandler(Socket socket, ChatServer server, String clientIP, String color) throws IOException {
        this.server = server;
        this.clientIP = clientIP;
        this.color = color;
        this.connectionManager = new ConnectionManager(socket);
        this.displayName = clientIP;  // Default display name is the client's IP
    }

    /**
     * Main thread loop for handling incoming messages from the client.
     * Continuously reads messages until the connection is closed.
     */
    @Override
    public void run() {
        try {
            String rawMessage;
            // Keep reading messages until the connection is closed
            while ((rawMessage = connectionManager.readLine()) != null) {
                processIncomingMessage(rawMessage);
            }
        } catch (IOException e) {
            // Connection was likely closed by the client
        } finally {
            // Ensure resources are cleaned up
            cleanup();
        }
    }

    /**
     * Processes a raw message received from the client.
     * Handles special messages like disconnection and typing indicators.
     *
     * @param rawMessage The raw message string received from the client
     */
    private void processIncomingMessage(String rawMessage) {
        try {
            // Parse the raw message into a ChatMessage object
            ChatMessage message = MessageParser.parseMessage(rawMessage);

            // Handle disconnection request
            if (message.getContent() != null && message.getContent().equals("DISCONNECT")) {
                clientInitiatedDisconnect = true;
                // Notify all clients about the disconnection
                ChatMessage disconnectMsg = ChatMessage.createTextMessage(
                        "SERVER",
                        AppConfig.SERVER_MESSAGE_COLOR,
                        "Client disconnected: " + (displayName != null ? displayName : clientIP)
                );
                server.broadcastMessage(disconnectMsg);
                cleanup();
                return;
            }

            // Set default sender information if not provided
            if (message.getSender() == null || message.getSender().equals("Client")) {
                if (message.isImageMessage()) {
                    message = ChatMessage.createImageMessage(clientIP, color, 
                            message.getFileName(), message.getImageData());
                } else {
                    message = ChatMessage.createTextMessage(clientIP, color, message.getContent());
                }
            }

            // Handle first-time client announcement
            if (!announced && message.getSender() != null && !message.getSender().equals("Client")) {
                // Handle JOIN message (legacy format)
                if (message.isTextMessage() && "[JOIN]".equals(message.getContent())) {
                    displayName = message.getSender();
                    announced = true;
                    // Notify all clients about the new connection
                    ChatMessage serverMsg = ChatMessage.createTextMessage(
                            "SERVER",
                            AppConfig.SERVER_MESSAGE_COLOR,
                            "Client connected: " + displayName
                    );
                    server.broadcastMessage(serverMsg);
                    server.displayMessage(serverMsg);
                    return;
                } else {
                    // Standard connection announcement
                    displayName = message.getSender();
                    announced = true;
                    // Notify all clients about the new connection
                    server.broadcastMessage(ChatMessage.createTextMessage(
                            "SERVER",
                            AppConfig.SERVER_MESSAGE_COLOR,
                            "Client connected: " + displayName
                    ));
                    server.displayMessage(ChatMessage.createTextMessage(
                            "SERVER",
                            AppConfig.SERVER_MESSAGE_COLOR,
                            "Client connected: " + displayName
                    ));
                }
            }

            // Handle typing indicator
            if (message.isTextMessage() && "[TYPING]".equals(message.getContent())) {
                server.broadcastMessage(message);  // Forward typing indicator to all clients
                return;
            }

            // Display and broadcast the message
            server.displayMessage(message);  // Show in server console
            server.broadcastMessage(message);  // Send to all connected clients

        } catch (Exception e) {
            // Log or handle message processing errors
        }
    }

    /**
     * Sends a message to the connected client.
     *
     * @param message The message to send
     */
    public void sendMessage(ChatMessage message) {
        if (connectionManager != null && connectionManager.isConnected()) {
            // Format the message for network transmission
            String formattedMessage = MessageFormatter.formatForTransmission(message);
            // Send the formatted message
            connectionManager.sendMessage(formattedMessage);
        }
    }

    /**
     * Cleans up resources and notifies about client disconnection.
     * Called when the client disconnects or an error occurs.
     */
    private void cleanup() {
        // Close the network connection
        if (connectionManager != null) {
            connectionManager.close();
        }
        
        // Remove this client from the server's active clients list
        server.removeClient(this);
        
        // If the client initiated the disconnection, notify other clients
        if (clientInitiatedDisconnect) {
            server.displayMessage(
                ChatMessage.createTextMessage(
                    "SERVER", 
                    AppConfig.SERVER_MESSAGE_COLOR,
                    "Client disconnected: " + (displayName != null ? displayName : clientIP)
                )
            );
        }
    }
}
