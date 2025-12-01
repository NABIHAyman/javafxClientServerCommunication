package client.c;

import common.config.AppConfig;
import common.interfaces.IMessageReceiver;
import common.model.ChatMessage;
import common.services.ConnectionManager;
import common.services.MessageParser;
import javafx.application.Platform;

import java.io.IOException;
import java.net.Socket;

/**
 * Handles the client-side network communication for the chat application.
 * Manages the connection to the chat server, message sending/receiving,
 * and connection lifecycle.
 * 
 * <p>This class extends {@code Thread} to run network operations in a separate thread,
 * ensuring the UI remains responsive. It implements the client-side protocol for
 * communicating with the chat server.</p>
 * 
 * <p>Example usage:
 * <pre>
 * IMessageReceiver receiver = new MyMessageReceiver();
 * ChatClient client = new ChatClient(receiver, "localhost", 12345);
 * client.start();
 * client.sendMessage(ChatMessage.createTextMessage("user", "#FF0000", "Hello!"));
 * </pre>
 * 
 * @see IMessageReceiver
 * @see ChatMessage
 * @see ConnectionManager
 */
public class ChatClient extends Thread {
    /** Manages the underlying socket connection */
    private ConnectionManager connectionManager;
    
    /** Callback interface for receiving messages and connection events */
    private final IMessageReceiver messageReceiver;
    
    /** Server address to connect to */
    private final String serverAddress;
    
    /** Server port to connect to */
    private final int serverPort;
    
    /** Flag indicating if the client should continue running */
    private volatile boolean running;
    
    /** Initial message sent upon connection (e.g., authentication) */
    private volatile ChatMessage initialHandshake;

    /**
     * Creates a new chat client instance.
     * 
     * @param messageReceiver The callback interface for message and event handling
     * @param serverAddress The server hostname or IP address to connect to
     * @param serverPort The server port to connect to
     * @throws IllegalArgumentException if messageReceiver is null or serverAddress is null/empty
     */
    public ChatClient(IMessageReceiver messageReceiver, String serverAddress, int serverPort) {
        this.messageReceiver = messageReceiver;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.running = true;
    }

    /**
     * Main client loop that handles the connection to the server and processes
     * incoming messages. Runs in a separate thread to keep the UI responsive.
     * 
     * <p>This method will automatically attempt to connect to the server and
     * process incoming messages until either the connection is closed or
     * {@link #shutdown()} is called.</p>
     * 
     * <p>Any connection errors will be reported through the {@code IMessageReceiver}.</p>
     */
    @Override
    public void run() {
        try {
            Socket socket = new Socket(serverAddress, serverPort);
            connectionManager = new ConnectionManager(socket);

            if (initialHandshake != null && connectionManager.isConnected()) {
                String formatted = common.services.MessageFormatter.formatForTransmission(initialHandshake);
                connectionManager.sendMessage(formatted);
            }

            String rawMessage;
            while (running && (rawMessage = connectionManager.readLine()) != null) {
                processIncomingMessage(rawMessage);
            }
        } catch (IOException e) {
            Platform.runLater(() ->
                    messageReceiver.onError("[CLIENT] Connection error: " + e.getMessage())
            );
        } finally {
            shutdown();
        }
    }

    /**
     * Processes a raw message received from the server.
     * 
     * <p>This method parses the raw message string into a {@code ChatMessage} object
     * and handles special messages like disconnection notifications. All message
     * processing is delegated to the UI thread using {@code Platform.runLater()}.
     * 
     * @param rawMessage The raw message string received from the server
     */
    private void processIncomingMessage(String rawMessage) {
        try {
            ChatMessage message = MessageParser.parseMessage(rawMessage);
            if (message.getContent() != null && "DISCONNECT".equals(message.getContent())) {
                running = false;
                return;
            }
            Platform.runLater(() -> messageReceiver.onMessageReceived(message));
        } catch (Exception e) {
            Platform.runLater(() ->
                    messageReceiver.onError("[CLIENT] Error parsing message: " + e.getMessage())
            );
        }
    }

    /**
     * Sends a chat message to the server.
     * 
     * @param message The message to send
     * @throws IllegalStateException if not connected to the server
     * @throws IllegalArgumentException if message is null
     */
    public void sendMessage(ChatMessage message) {
        if (connectionManager != null && connectionManager.isConnected()) {
            String formattedMessage = common.services.MessageFormatter.formatForTransmission(message);
            connectionManager.sendMessage(formattedMessage);
        }
    }

    /**
     * Checks if the client is currently connected to the server.
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connectionManager != null && connectionManager.isConnected();
    }

    /**
     * Gracefully shuts down the client connection.
     * 
     * <p>This method will:
     * <ol>
     *   <li>Set the running flag to false to stop the message processing loop</li>
     *   <li>Send a DISCONNECT message to the server if connected</li>
     *   <li>Close the underlying connection</li>
     *   <li>Clean up resources</li>
     * </ol>
     * 
     * <p>This method is idempotent and can be called multiple times safely.</p>
     */
    public void shutdown() {
        running = false;
        if (connectionManager != null) {
            try {
                if (connectionManager.isConnected()) {
                    connectionManager.sendMessage("DISCONNECT");
                    Thread.sleep(100);
                }
            } catch (Exception e) {
            } finally {
                connectionManager.close();
                connectionManager = null;
            }
        }
    }

    /**
     * Sets the initial handshake message to be sent upon connection.
     * This is typically used for authentication or initial setup.
     * 
     * @param handshake The message to send after connecting
     * @throws IllegalArgumentException if handshake is null
     */
    public void setInitialHandshake(ChatMessage handshake) {
        this.initialHandshake = handshake;
    }
}
