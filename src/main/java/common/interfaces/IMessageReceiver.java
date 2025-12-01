package common.interfaces;

import common.model.ChatMessage;

/**
 * Defines callbacks for handling incoming messages, connection events, and errors
 * in a chat application. Implementations of this interface can receive and process
 * real-time updates from a chat server.
 * 
 * <p>This interface is typically implemented by UI controllers or services that need
 * to react to chat events and update the user interface accordingly.</p>
 * 
 * <p>All callback methods should be thread-safe and handle any necessary
 * synchronization or thread marshaling (e.g., Platform.runLater() for JavaFX).</p>
 */
public interface IMessageReceiver {
    
    /**
     * Called when a new chat message is received.
     * 
     * @param message The received chat message containing sender, content, and metadata
     * @throws IllegalArgumentException if message is null
     */
    void onMessageReceived(ChatMessage message);
    
    /**
     * Called when an error occurs in the communication channel.
     * 
     * @param error A description of the error that occurred
     * @throws IllegalArgumentException if error is null or empty
     */
    void onError(String error);
    
    /**
     * Called when a new user connects to the chat.
     * 
     * @param username The username of the newly connected user
     * @throws IllegalArgumentException if username is null or empty
     */
    void onUserConnected(String username);
}


