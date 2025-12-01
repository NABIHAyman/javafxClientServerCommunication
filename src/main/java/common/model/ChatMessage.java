package common.model;

/**
 * Represents a message in the chat system, which can be either a text message or an image message.
 * This is an immutable data class that encapsulates all information needed to display a message
 * in the chat interface, including sender information, message content, and styling.
 * 
 * <p>This class uses the Factory Method pattern with static factory methods for creating
 * different types of messages.</p>
 * 
 * <p>Example usage:
 * <pre>
 * // Create a text message
 * ChatMessage textMsg = ChatMessage.createTextMessage("user1", "#FF0000", "Hello!");
 * 
 * // Create an image message
 * ChatMessage imgMsg = ChatMessage.createImageMessage("user2", "#0000FF", "image.png", base64Data);
 * </pre>
 */
public class ChatMessage {
    /** Type of the message (TEXT or IMAGE) */
    private final MessageType type;
    
    /** Username of the message sender */
    private final String sender;
    
    /** Color code for the sender's username, in CSS format (e.g., "#RRGGBB") */
    private final String color;
    
    /** The text content of the message (only for TEXT type) */
    private final String content;
    
    /** Original filename of the image (only for IMAGE type) */
    private final String fileName;
    
    /** Base64-encoded image data (only for IMAGE type) */
    private final String imageData;

    /**
     * Private constructor. Use factory methods to create instances.
     * 
     * @param type The type of the message (TEXT or IMAGE)
     * @param sender The username of the message sender
     * @param color The color code for the sender's username
     * @param content The text content (for TEXT messages) or null (for IMAGE messages)
     * @param fileName The original filename (for IMAGE messages) or null (for TEXT messages)
     * @param imageData Base64-encoded image data (for IMAGE messages) or null (for TEXT messages)
     */
    private ChatMessage(MessageType type, String sender, String color, String content, String fileName, String imageData) {
        this.type = type;
        this.sender = sender;
        this.color = color;
        this.content = content;
        this.fileName = fileName;
        this.imageData = imageData;
    }

    /**
     * Creates a new text message.
     *
     * @param sender The username of the message sender
     * @param color The color code for the sender's username (e.g., "#RRGGBB")
     * @param content The text content of the message
     * @return A new ChatMessage instance of type TEXT
     * @throws IllegalArgumentException if sender or content is null or empty, or if color is invalid
     */
    public static ChatMessage createTextMessage(String sender, String color, String content) {
        return new ChatMessage(MessageType.TEXT, sender, color, content, null, null);
    }

    /**
     * Creates a new image message.
     *
     * @param sender The username of the message sender
     * @param color The color code for the sender's username (e.g., "#RRGGBB")
     * @param fileName The original filename of the image
     * @param imageData Base64-encoded image data
     * @return A new ChatMessage instance of type IMAGE
     * @throws IllegalArgumentException if any parameter is null or empty, or if color is invalid
     */
    public static ChatMessage createImageMessage(String sender, String color, String fileName, String imageData) {
        return new ChatMessage(MessageType.IMAGE, sender, color, null, fileName, imageData);
    }

    /**
     * Gets the type of the message.
     * 
     * @return The message type (TEXT or IMAGE)
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Gets the username of the message sender.
     * 
     * @return The sender's username
     */
    public String getSender() {
        return sender;
    }

    /**
     * Gets the color code for the sender's username.
     * 
     * @return The color code in CSS format (e.g., "#RRGGBB")
     */
    public String getColor() {
        return color;
    }

    /**
     * Gets the text content of the message.
     * 
     * @return The message content for TEXT messages, or null for IMAGE messages
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the original filename of the image.
     * 
     * @return The filename for IMAGE messages, or null for TEXT messages
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the Base64-encoded image data.
     * 
     * @return The image data for IMAGE messages, or null for TEXT messages
     */
    public String getImageData() {
        return imageData;
    }

    /**
     * Checks if this is an image message.
     * 
     * @return true if this is an IMAGE message, false otherwise
     */
    public boolean isImageMessage() {
        return type == MessageType.IMAGE;
    }

    /**
     * Checks if this is a text message.
     * 
     * @return true if this is a TEXT message, false otherwise
     */
    public boolean isTextMessage() {
        return type == MessageType.TEXT;
    }
}


