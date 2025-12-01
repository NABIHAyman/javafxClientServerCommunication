package common.interfaces;

/**
 * Defines the contract for display services that handle the presentation of
 * text and images in the chat interface.
 * 
 * <p>Implementations of this interface are responsible for rendering messages
 * and images in a thread-safe manner, typically by delegating to the JavaFX
 * Application Thread when updating the UI.</p>
 * 
 * <p>This interface allows for different display implementations while maintaining
 * a consistent API for the rest of the application.</p>
 */
public interface IDisplayService {
    
    /**
     * Displays a text message with the specified color in the chat interface.
     * The implementation should handle any necessary formatting, such as emoji
     * substitution or text styling.
     *
     * @param message The text message to display
     * @param color   The color to use for the message text, in CSS color format (e.g., "#RRGGBB")
     * @throws IllegalArgumentException if message is null or color is in an invalid format
     */
    void displayText(String message, String color);
    
    /**
     * Displays an image in the chat interface with an optional label.
     * The implementation should handle image loading, scaling, and any error cases.
     *
     * @param label     A label or caption for the image (may be null or empty)
     * @param imagePath The filesystem path to the image file
     * @param color     The color to use for the label text, in CSS color format (e.g., "#RRGGBB")
     * @throws IllegalArgumentException if imagePath is null, empty, or points to a non-existent file
     * @throws IllegalStateException if the image cannot be loaded or displayed
     */
    void displayImage(String label, String imagePath, String color);
}


