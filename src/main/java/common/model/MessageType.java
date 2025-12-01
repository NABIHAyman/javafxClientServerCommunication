package common.model;

/**
 * Enumerates the types of messages that can be sent in the chat system.
 * This enum is used by the {@link ChatMessage} class to distinguish between
 * different types of chat messages and handle them appropriately.
 * 
 * <p>The supported message types are:
 * <ul>
 *   <li>{@link #TEXT} - A standard text message containing plain or formatted text</li>
 *   <li>{@link #IMAGE} - A message containing an image with optional text caption</li>
 * </ul>
 * 
 * @see ChatMessage
 */
public enum MessageType {
    /**
     * Represents a standard text message that may contain plain or formatted text.
     * Text messages can include emoji shortcodes, which will be replaced with
     * their corresponding Unicode characters or images when displayed.
     */
    TEXT,
    
    /**
     * Represents a message containing an image, optionally with a text caption.
     * Image messages include the image data (typically as Base64-encoded string)
     * and may include a filename and/or description.
     */
    IMAGE
}


