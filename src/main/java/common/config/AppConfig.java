package common.config;

/**
 * Centralized configuration class containing application-wide constants.
 * This class provides a single source of truth for all configurable parameters
 * used throughout the chat application.
 * 
 * <p>This class cannot be instantiated and contains only static final fields.</p>
 * 
 * <p>Example usage:
 * <pre>
 * // Using a configuration value
 * int port = AppConfig.SERVER_PORT;
 * String[] imageTypes = AppConfig.IMAGE_EXTENSIONS;
 * </pre>
 */
public final class AppConfig {

    // Prevent instantiation - all members are static
    private AppConfig() {}

    /** Default port number for the chat server */
    public static final int SERVER_PORT = 12345;

    /** Default width for displayed images in pixels */
    public static final int IMAGE_WIDTH = 300;
    
    /** Default height for displayed images in pixels */
    public static final int IMAGE_HEIGHT = 300;

    /** Color for server messages in the chat (green) */
    public static final String SERVER_MESSAGE_COLOR = "#4CAF50";
    
    /** Default color for client messages in the chat (light gray) */
    public static final String CLIENT_MESSAGE_COLOR = "#E0E0E0";
    
    /** Color for error messages in the chat (red) */
    public static final String ERROR_COLOR = "#F44336";
    
    /** Color for the start server button (orange) */
    public static final String SERVER_BUTTON_START = "#FF9800";
    
    /** Color for the stop server button (red) */
    public static final String SERVER_BUTTON_STOP = "#F44336";

    /** 
     * Array of supported image file extensions for file chooser filters.
     * Used when selecting images to send in the chat.
     * 
     * <p>Supported formats:
     * <ul>
     *   <li>PNG (.png)</li>
     *   <li>JPEG (.jpg, .jpeg)</li>
     *   <li>GIF (.gif)</li>
     *   <li>BMP (.bmp)</li>
     * </ul>
     */
    public static final String[] IMAGE_EXTENSIONS = {"*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"};
}
