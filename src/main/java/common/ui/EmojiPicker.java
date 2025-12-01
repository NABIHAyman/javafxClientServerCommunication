package common.ui;

// Service imports
import common.services.EmojiService;

// JavaFX geometry and layout
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;

// JavaFX UI controls
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

// JavaFX graphics and text
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

// JavaFX window management
import javafx.stage.Screen;
import javafx.stage.Window;

// Java collections and functional interfaces
import java.util.Map;
import java.util.function.Consumer;

/**
 * A customizable emoji picker component that displays a grid of emojis.
 * When an emoji is selected, it invokes the provided callback with the selected emoji.
 * Handles emoji rendering with proper font fallbacks and tooltip support.
 */
public class EmojiPicker {
    // UI Configuration
    private static final int COLUMN_COUNT = 5;  // Number of emoji columns in the grid
    
    // UI Components
    private final ContextMenu contextMenu;      // The popup menu containing emojis
    
    // Callback
    private final Consumer<String> onEmojiSelected;  // Called when an emoji is selected

    /**
     * Creates a new EmojiPicker with the specified selection handler.
     *
     * @param onEmojiSelected The callback to be invoked when an emoji is selected.
     *                        Receives the emoji character as a string.
     */
    public EmojiPicker(Consumer<String> onEmojiSelected) {
        this.onEmojiSelected = onEmojiSelected;
        this.contextMenu = buildContextMenu();
    }

    /**
     * Builds and configures the context menu that contains the emoji grid.
     * 
     * @return A fully configured ContextMenu containing all available emojis
     */
    private ContextMenu buildContextMenu() {
        // Create and style the context menu
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("emoji-picker");

        // Define font stack with emoji support for multiple platforms
        String emojiFonts = "'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji', 'Apple Color Emoji', 'Android Emoji', 'EmojiSymbols', 'Symbola', 'Arial Unicode MS', 'sans-serif';";
        
        // Apply consistent styling to the menu
        menu.setStyle(
            "-fx-font-family: " + emojiFonts + " " +
            "-fx-font-size: 18px;"
        );
        
        // Create a grid to hold the emoji buttons
        GridPane grid = new GridPane();
        grid.setHgap(8);  // Horizontal gap between emoji buttons
        grid.setVgap(8);  // Vertical gap between emoji buttons
        grid.setPadding(new Insets(10));  // Padding around the grid
        
        // Style the grid with transparent background and emoji fonts
        grid.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-font-family: " + emojiFonts
        );
        
        // Test if emoji rendering works with the current font configuration
        javafx.scene.control.Label testLabel = new javafx.scene.control.Label("❤️");
        testLabel.setStyle(
            "-fx-font-family: " + emojiFonts + " " +
            "-fx-font-size: 24px;"
        );
        
        // Check if the test emoji renders correctly or shows a fallback character
        boolean useUnicodeFallback = testLabel.getText().isEmpty() ||
                                   testLabel.getText().equals("□") ||  // Common fallback character
                                   testLabel.getText().equals("");

        // Initialize grid position counters
        int col = 0;
        int row = 0;
        
        // Get the emoji map from the service
        Map<String, String> unicodeMap = EmojiService.getEmojiMap();
        
        // Create a button for each emoji in the map
        for (Map.Entry<String, String> entry : unicodeMap.entrySet()) {
            // Create a text node for the emoji
            Text emojiText = new Text(entry.getValue());
            emojiText.setStyle(
                "-fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji', 'Apple Color Emoji', 'Android Emoji', 'EmojiSymbols', 'Symbola';" +
                "-fx-font-size: 24px;" +
                "-fx-fill: #FFFFFF;"  // White text color for better visibility
            );
            emojiText.setTextOrigin(javafx.geometry.VPos.CENTER);
            emojiText.setBoundsType(TextBoundsType.VISUAL);
            
            // Create the button that will display the emoji
            Button emojiButton = new Button();
            emojiButton.setGraphic(emojiText);
            emojiButton.getStyleClass().add("emoji-option-button");
            
            // Set fixed button size and disable focus traversal
            emojiButton.setMinSize(44, 44);
            emojiButton.setPrefSize(44, 44);
            emojiButton.setMaxSize(44, 44);
            emojiButton.setFocusTraversable(false);
            
            // Configure button appearance
            emojiButton.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
            emojiButton.setAlignment(javafx.geometry.Pos.CENTER);
            emojiButton.setGraphicTextGap(0);
            emojiButton.setPickOnBounds(true);
            
            // Add clipping to prevent rendering artifacts
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(emojiButton.widthProperty());
            clip.heightProperty().bind(emojiButton.heightProperty());
            emojiButton.setClip(clip);
            
            // Style the button with transparent background
            emojiButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-padding: 8px;"
            );
            
            // Add tooltip showing the emoji's shortcode
            Tooltip tooltip = new Tooltip(entry.getKey());
            tooltip.setStyle(
                "-fx-font-size: 14px; " +
                "-fx-font-family: 'Segoe UI', Arial, sans-serif;"
            );
            Tooltip.install(emojiButton, tooltip);

            // Handle emoji selection
            emojiButton.setOnAction(event -> {
                if (onEmojiSelected != null) {
                    onEmojiSelected.accept(entry.getValue());
                }
                menu.hide();  // Close the picker after selection
            });

            // Add mouse event handlers for hover effects
            emojiButton.setOnMouseEntered(e -> {
                // Style the button with a blue background on hover
                emojiButton.setStyle(
                    "-fx-font-family: 'Segoe UI Emoji', 'Noto Color Emoji', 'Apple Color Emoji', 'Android Emoji', 'EmojiSymbols', 'Symbola';" +
                    "-fx-font-size: 24px;" +
                    "-fx-text-fill: #FFFFFF;" +
                    "-fx-background-color: rgba(90, 159, 212, 0.3);" +
                    "-fx-padding: 8px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-scale-x: 1; -fx-scale-y: 1;"
                );
            });
            
            // Reset button style when mouse exits
            emojiButton.setOnMouseExited(e -> {
                emojiButton.setStyle(
                    "-fx-font-family: 'Segoe UI Emoji', 'Noto Color Emoji', 'Apple Color Emoji', 'Android Emoji', 'EmojiSymbols', 'Symbola';" +
                    "-fx-font-size: 24px;" +
                    "-fx-text-fill: #FFFFFF;" +
                    "-fx-background-color: transparent;" +  // Transparent background by default
                    "-fx-padding: 8px;" +
                    "-fx-scale-x: 1; -fx-scale-y: 1;"      // Normal scale
                );
            });
            
            // Handle emoji selection
            emojiButton.setOnAction(event -> {
                // Notify the callback about the selected emoji
                onEmojiSelected.accept(entry.getValue());
                // Hide the picker after selection
                menu.hide();
            });
            
            // Add the button to the grid at the current position
            grid.add(emojiButton, col, row);
            
            // Move to the next grid position
            col++;
            if (col >= COLUMN_COUNT) {
                // Move to the next row when the current row is full
                col = 0;
                row++;
            }
        }

        // Create a custom menu item containing the emoji grid
        CustomMenuItem menuItem = new CustomMenuItem(grid);
        menuItem.setHideOnClick(false);  // Keep menu open after clicking an emoji
        menu.getItems().add(menuItem);   // Add the grid to the context menu
        
        return menu;  // Return the fully configured context menu
    }

    /**
     * Toggles the visibility of the emoji picker at the center of the owner node.
     * If the picker is already visible, it will be hidden. If hidden, it will be shown.
     *
     * @param owner The node relative to which the picker should be positioned
     */
    public void toggle(Node owner) {
        if (contextMenu.isShowing()) {
            // Hide the picker if it's currently showing
            contextMenu.hide();
        } else {
            // Show the picker initially at (0,0) to calculate its size
            contextMenu.show(owner, 0, 0);
            
            // Get the window containing the owner node
            Window window = owner.getScene().getWindow();
            
            // Calculate center position relative to the owner window
            double centerX = window.getX() + (window.getWidth() / 2) - (contextMenu.getWidth() / 2);
            double centerY = window.getY() + (window.getHeight() / 2) - (contextMenu.getHeight() / 2);

            // Get the screen where the owner window is located
            Screen screen = Screen.getScreensForRectangle(
                    window.getX(), window.getY(),
                    window.getWidth(), window.getHeight()
            ).get(0);

            // Get the visual bounds of the screen (accounting for taskbars, etc.)
            Rectangle2D screenBounds = screen.getVisualBounds();

            // Ensure the picker stays within screen bounds
            centerX = Math.max(screenBounds.getMinX(),
                    Math.min(centerX,
                            screenBounds.getMaxX() - contextMenu.getWidth()));

            centerY = Math.max(screenBounds.getMinY(),
                    Math.min(centerY,
                            screenBounds.getMaxY() - contextMenu.getHeight()));

            // Hide and show again at the calculated position
            contextMenu.hide();
            contextMenu.show(owner, centerX, centerY);
        }
    }

    /**
     * Hides the emoji picker if it's currently showing.
     * This is a convenience method that delegates to the underlying context menu.
     */
    public void hide() {
        contextMenu.hide();
    }
}
