package common.services;

import common.model.ChatMessage;

/**
 * Formats ChatMessage objects into various string representations.
 * Handles both text and image messages for different display and transmission needs.
 * 
 * <p>Supports two main formatting modes:
 * <ul>
 *   <li>Transmission format: For sending over network (e.g., "sender|color|message")</li>
 *   <li>Display format: For UI presentation (e.g., "[sender] : message")</li>
 * </ul>
 */
public class MessageFormatter {
    // Format constants for message serialization
    private static final String IMAGE_START_MARKER = "[IMG]";
    private static final String IMAGE_END_MARKER = "[/IMG]";
    private static final String METADATA_SEPARATOR = "|";

    // Prevent instantiation - all methods are static
    private MessageFormatter() {}

    /**
     * Formats a ChatMessage for network transmission.
     * Chooses the appropriate format based on message type (text/image).
     *
     * @param message The message to format
     * @return A string representation suitable for network transmission
     */
    public static String formatForTransmission(ChatMessage message) {
        if (message.isImageMessage()) {
            return formatImageMessage(message);
        }
        return formatTextMessage(message);
    }

    /**
     * Formats an image message for transmission.
     * Format: [IMG]sender|color|filename[/IMG]base64data
     *
     * @param message The image message to format
     * @return Formatted image message string
     */
    private static String formatImageMessage(ChatMessage message) {
        // Combine metadata fields with separator
        String metadata = String.join(METADATA_SEPARATOR,
            message.getSender(),
            message.getColor(),
            message.getFileName()
        );
        
        // Combine all parts into final format
        return IMAGE_START_MARKER + metadata + IMAGE_END_MARKER + message.getImageData();
    }

    /**
     * Formats a text message for transmission.
     * Format: "sender|color|message" or just "message" for system messages
     *
     * @param message The text message to format
     * @return Formatted text message string
     */
    private static String formatTextMessage(ChatMessage message) {
        // Include metadata if sender and color are available
        if (message.getSender() != null && message.getColor() != null) {
            return String.join(METADATA_SEPARATOR,
                message.getSender(),
                message.getColor(),
                message.getContent()
            );
        }
        // Return just the content for system messages
        return message.getContent();
    }

    /**
     * Formats a message for display in the UI.
     * For text messages: "[sender] : message" or just "message" for system messages
     * For image messages: "[sender] Image: filename"
     *
     * @param message The message to format
     * @return A user-friendly string representation of the message
     */
    public static String formatForDisplay(ChatMessage message) {
        if (message.isTextMessage()) {
            // Format regular text messages with sender info if available
            if (message.getSender() != null) {
                return String.format("[%s] : %s", message.getSender(), message.getContent());
            }
            // System messages (no sender) are shown as-is
            return message.getContent();
        } else {
            // Format image messages with sender and filename
            return String.format("[%s] Image: %s", message.getSender(), message.getFileName());
        }
    }
}


