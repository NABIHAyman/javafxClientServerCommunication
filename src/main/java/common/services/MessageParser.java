package common.services;

import common.model.ChatMessage;

/**
 * Parses raw message strings into ChatMessage objects.
 * Handles both text and image message formats with metadata.
 * 
 * <p>Message formats:
 * <ul>
 *   <li>Text: "sender|color|message" or just "message" for system messages</li>
 *   <li>Image: "[IMG]sender|color|filename[/IMG]base64data"</li>
 * </ul>
 */
public class MessageParser {
    // Format constants for message parsing
    private static final String IMAGE_START_MARKER = "[IMG]";
    private static final String IMAGE_END_MARKER = "[/IMG]";
    private static final String METADATA_SEPARATOR = "\\|";
    private static final int METADATA_PARTS_COUNT = 3;  // sender, color, content/filename

    // Prevent instantiation - all methods are static
    private MessageParser() {}

    /**
     * Checks if a message is an image message based on its format.
     *
     * @param message The message to check
     * @return true if the message is an image message, false otherwise
     */
    public static boolean isImageMessage(String message) {
        return message != null && message.startsWith(IMAGE_START_MARKER);
    }

    /**
     * Parses a raw message string into a ChatMessage object.
     * Automatically detects and handles both text and image messages.
     *
     * @param rawMessage The raw message string to parse
     * @return A ChatMessage object representing the parsed message
     * @throws IllegalArgumentException if the message format is invalid
     */
    public static ChatMessage parseMessage(String rawMessage) {
        if (isImageMessage(rawMessage)) {
            return parseImageMessage(rawMessage);
        }
        return parseTextMessage(rawMessage);
    }

    /**
     * Parses an image message string into a ChatMessage object.
     * Expects format: [IMG]sender|color|filename[/IMG]base64data
     *
     * @param message The image message string to parse
     * @return A ChatMessage object with image data
     * @throws IllegalArgumentException if the message format is invalid
     */
    private static ChatMessage parseImageMessage(String message) {
        // Find the end of the image metadata section
        int endMarkerIdx = message.indexOf(IMAGE_END_MARKER);
        if (endMarkerIdx <= IMAGE_START_MARKER.length()) {
            throw new IllegalArgumentException("Invalid image message format");
        }

        // Extract metadata and image data
        String metadata = message.substring(IMAGE_START_MARKER.length(), endMarkerIdx);
        String imageData = message.substring(endMarkerIdx + IMAGE_END_MARKER.length());

        // Parse metadata parts (sender, color, filename)
        String[] parts = metadata.split(METADATA_SEPARATOR, METADATA_PARTS_COUNT);
        if (parts.length >= METADATA_PARTS_COUNT) {
            // Full metadata available
            String sender = parts[0];
            String color = parts[1];
            String fileName = parts[2];
            return ChatMessage.createImageMessage(sender, color, fileName, imageData);
        } else if (parts.length == 1) {
            // Only sender provided, use defaults for other fields
            return ChatMessage.createImageMessage(parts[0], null, parts[0], imageData);
        } else {
            throw new IllegalArgumentException("Invalid image message metadata format");
        }
    }

    /**
     * Parses a text message string into a ChatMessage object.
     * Handles both simple messages and messages with metadata.
     *
     * @param message The text message to parse
     * @return A ChatMessage object with the parsed message
     */
    private static ChatMessage parseTextMessage(String message) {
        // Check if message contains metadata (sender|color|content)
        if (message.contains("|")) {
            String[] parts = message.split(METADATA_SEPARATOR, METADATA_PARTS_COUNT);
            if (parts.length == METADATA_PARTS_COUNT) {
                // Extract metadata and create message
                String sender = parts[0];
                String color = parts[1];
                String content = parts[2];
                return ChatMessage.createTextMessage(sender, color, content);
            }
        }
        // Return as system message (no sender/color)
        return ChatMessage.createTextMessage(null, null, message);
    }
}

