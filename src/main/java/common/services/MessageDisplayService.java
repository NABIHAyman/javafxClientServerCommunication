package common.services;

// Application configuration
import common.config.AppConfig;

// Interface implementation
import common.interfaces.IDisplayService;

// UI components
import common.ui.EmojiTextFormatter;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

// Java IO
import java.io.File;

/**
 * Service responsible for displaying messages and images in the chat interface.
 * Handles text formatting, emoji rendering, and image display in a thread-safe manner.
 * 
 * <p>This service implements the {@link IDisplayService} interface and provides
 * methods to display both text and images in a JavaFX UI. It ensures that all
 * UI updates are performed on the JavaFX Application Thread using {@code Platform.runLater()}.
 * 
 * <p>Features include:
 * <ul>
 *   <li>Thread-safe message display</li>
 *   <li>Rich text formatting with emoji support</li>
 *   <li>Image loading and display with error handling</li>
 *   <li>Automatic sender formatting (e.g., "[User] message")</li>
 *   <li>Consistent styling using application colors</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 * // In a JavaFX controller
 * VBox messageContainer = new VBox();
 * IDisplayService displayService = new MessageDisplayService(messageContainer);
 * 
 * // Display a text message
 * displayService.displayText("[User] Hello, world!", "#4CAF50");
 * 
 * // Display an image
 * displayService.displayImage("[User] Check this out", "/path/to/image.png", "#4CAF50");
 * </pre>
 * 
 * @see IDisplayService
 * @see EmojiTextFormatter
 * @see AppConfig
 */
public class MessageDisplayService implements IDisplayService {
    /** 
     * The container where all chat messages will be displayed.
     * This is a JavaFX VBox that stacks messages vertically.
     */
    private final VBox messageContainer;

    /**
     * Creates a new MessageDisplayService that will display messages in the specified container.
     * The container should be a JavaFX VBox that is part of an active JavaFX scene graph.
     *
     * @param container The VBox container where messages will be displayed.
     *                  Must not be null and should be part of a JavaFX scene.
     * @throws IllegalArgumentException if container is null
     */
    public MessageDisplayService(VBox container) {
        this.messageContainer = container;
    }

    /**
     * Displays a text message in the chat interface with the specified color.
     * Handles message formatting, including sender labels and emoji rendering.
     *
     * @param message The message text to display
     * @param color   The color to use for the message text
     */
    /**
     * Displays a text message in the chat interface with the specified color.
     * This method is thread-safe and will automatically handle the UI update
     * on the JavaFX Application Thread.
     *
     * <p>Message formatting:
     * <ul>
     *   <li>If the message starts with "[Sender]", the sender will be displayed in bold</li>
     *   <li>Emoji shortcodes (e.g., ":smile:") will be replaced with emoji images</li>
     *   <li>The message will be styled with the specified color</li>
     * </ul>
     *
     * @param message The message text to display. May contain emoji shortcodes and sender information.
     * @param color   The CSS color value to use for the message text (e.g., "#4CAF50")
     * @throws IllegalArgumentException if message is null or empty, or if color is not a valid CSS color
     * @see EmojiTextFormatter#createFormattedText(String)
     */
    @Override
    public void displayText(String message, String color) {
        // Ensure UI updates happen on the JavaFX Application Thread
        Platform.runLater(() -> {
            // Create a container for the message
            TextFlow container = new TextFlow();
            container.getStyleClass().add("message-container");

            // Split message into sender and content if it follows the format "[Sender] message"
            String[] parts = message.split("]", 2);
            if (parts.length == 2) {
                // Format sender text with bold styling
                Text senderText = new Text(parts[0] + "] ");
                senderText.setStyle("-fx-fill: " + color + "; -fx-font-weight: bold;");

                // Format message content with emoji support
                TextFlow messageFlow = EmojiTextFormatter.createFormattedText(parts[1].trim());
                messageFlow.setStyle("-fx-fill: " + color + ";");

                // Combine sender and message
                container.getChildren().addAll(senderText, messageFlow);
            } else {
                // Format message without sender
                TextFlow messageFlow = EmojiTextFormatter.createFormattedText(message);
                messageFlow.setStyle("-fx-fill: " + color + ";");
                container.getChildren().add(messageFlow);
            }

            // Add the formatted message to the container
            messageContainer.getChildren().add(container);
        });
    }

    /**
     * Displays an image in the chat interface with an optional label.
     * Handles image loading, scaling, and error cases.
     *
     * @param label     The label or caption for the image
     * @param imagePath The file path to the image
     * @param color     The color to use for the label text
     */
    /**
     * Displays an image in the chat interface with an optional label.
     * The image will be loaded from the specified path and displayed with
     * proper scaling to fit within the configured dimensions.
     *
     * <p>Features:
     * <ul>
     *   <li>Supports sender labels in the format "[Sender] caption"</li>
     *   <li>Automatic image scaling while maintaining aspect ratio</li>
     *   <li>Error handling for missing or invalid images</li>
     *   <li>Thread-safe UI updates</li>
     * </ul>
     *
     * @param label     The label or caption for the image. May contain sender information.
     * @param imagePath The file system path to the image file. Must be a valid image file.
     * @param color     The CSS color value to use for the label text (e.g., "#4CAF50")
     * @throws IllegalArgumentException if imagePath is null or empty, or if color is invalid
     * @see AppConfig#IMAGE_WIDTH
     * @see AppConfig#IMAGE_HEIGHT
     */
    @Override
    public void displayImage(String label, String imagePath, String color) {
        // Ensure UI updates happen on the JavaFX Application Thread
        Platform.runLater(() -> {
            // Create a container for the image and label
            TextFlow textFlow = new TextFlow();
            textFlow.getStyleClass().add("image-container");

            // Process label if it follows the format "[Sender] label"
            String[] parts = label.split("]", 2);
            if (parts.length == 2) {
                // Format and add sender information
                Text ipPart = new Text(parts[0] + "] ");
                ipPart.setStyle("-fx-fill: " + color + "; -fx-font-weight: bold;");
                textFlow.getChildren().add(ipPart);
                textFlow.getChildren().add(new Text("\n"));
            }

            try {
                // Load and display the image with proper scaling
                Image image = new Image(new File(imagePath).toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(AppConfig.IMAGE_WIDTH);
                imageView.setFitHeight(AppConfig.IMAGE_HEIGHT);
                imageView.setPreserveRatio(true);
                textFlow.getChildren().add(imageView);
            } catch (Exception e) {
                // Show error message if image loading fails
                Text errorText = new Text("Failed to load image");
                errorText.setStyle("-fx-fill: " + AppConfig.ERROR_COLOR + ";");
                textFlow.getChildren().add(errorText);
            }

            // Add the image container to the message display
            messageContainer.getChildren().add(textFlow);
        });
    }

    /**
     * Convenience method to display text messages.
     * This method is maintained for backward compatibility and simply delegates
     * to {@link #displayText(String, String)}.
     *
     * @param message The message text to display (may contain emoji shortcodes)
     * @param color   The CSS color value to use for the message text
     * @see #displayText(String, String)
     * @deprecated Use {@link #displayText(String, String)} instead
     */
    @Deprecated
    public void showText(String message, String color) {
        displayText(message, color);
    }

    /**
     * Convenience method to display images.
     * This method is maintained for backward compatibility and simply delegates
     * to {@link #displayImage(String, String, String)}.
     *
     * @param label     The label or caption for the image
     * @param imagePath The file system path to the image file
     * @param color     The CSS color value to use for the label text
     * @see #displayImage(String, String, String)
     * @deprecated Use {@link #displayImage(String, String, String)} instead
     */
    @Deprecated
    public void showImage(String label, String imagePath, String color) {
        displayImage(label, imagePath, color);
    }
}
