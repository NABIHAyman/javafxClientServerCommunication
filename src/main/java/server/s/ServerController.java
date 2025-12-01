package server.s;

// Core application imports
import common.config.AppConfig;
import common.interfaces.IDisplayService;
import common.model.ChatMessage;

// Service layer imports
import common.services.FileChooserService;
import common.services.ImageService;
import common.services.MessageDisplayService;
import common.services.EmojiService;

// UI component imports
import common.ui.EmojiPicker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;

// Java IO imports
import java.io.File;

/**
 * Main controller for the chat server interface.
 * Manages server lifecycle, message handling, and UI updates.
 */
public class ServerController {

    // UI Components
    @FXML
    private ScrollPane messageScrollPane;  // Scrollable area for chat messages

    @FXML
    private VBox messageContainer;         // Container for all chat messages

    @FXML
    private TextField messageField;        // Input field for new messages

    @FXML
    private Button sendButton;             // Button to send text messages

    @FXML
    private Button serverControlButton;    // Button to start/stop the server

    @FXML
    private Button sendImageButton;        // Button to send images

    @FXML
    private Button emojiPickerButton;      // Button to open emoji picker

    // Server state
    private ChatServer chatServer;         // Handles client connections and message broadcasting
    private boolean serverRunning = false; // Tracks if server is currently running
    private IDisplayService displayService; // Service for displaying messages in the UI
    private EmojiPicker emojiPicker;       // Component for emoji selection

    /**
     * Initializes the controller after FXML loading is complete.
     * Sets up UI components, event handlers, and initial state.
     */
    @FXML
    public void initialize() {
        // Disable input controls until server is started
        messageField.setDisable(true);
        sendButton.setDisable(true);
        sendImageButton.setDisable(true);
        if (emojiPickerButton != null) {
            emojiPickerButton.setDisable(true);
        }

        // Initialize display service for showing messages
        displayService = new MessageDisplayService(messageContainer);

        // Set up emoji support tooltips and UI hints
        String emojiSupportText = "Emoji shortcodes: " + EmojiService.getSupportedShortcodesDescription();
        Tooltip emojiTooltip = new Tooltip(emojiSupportText);
        messageField.setPromptText("Type a message (use :smile:, :heart:, ...)");
        messageField.setTooltip(emojiTooltip);
        sendButton.setTooltip(emojiTooltip);
        if (emojiPickerButton != null) {
            emojiPickerButton.setTooltip(new Tooltip("Emoji picker"));
        }

        // Initialize emoji picker with insert callback
        emojiPicker = new EmojiPicker(emoji -> insertEmojiAtCaret(messageField, emoji));
        if (emojiPickerButton != null) {
            emojiPickerButton.setOnAction(event -> emojiPicker.toggle(emojiPickerButton));
        }

        // Configure scroll pane behavior
        messageScrollPane.setFocusTraversable(true);
        messageScrollPane.setFitToWidth(true);
        messageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        messageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Handle mouse wheel scrolling
        messageScrollPane.setOnScroll(event -> {
            // Calculate scroll amount based on wheel rotation
            double deltaY = event.getDeltaY() * 0.5;
            double width = messageScrollPane.getContent().getBoundsInLocal().getWidth();
            double vValue = messageScrollPane.getVvalue();
            messageScrollPane.setVvalue(vValue + -deltaY / width);
        });

        // Handle touchpad scrolling
        messageScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            // Smooth scrolling for touchpad input
            double deltaY = event.getDeltaY();
            double height = messageScrollPane.getContent().getBoundsInLocal().getHeight();
            double vvalue = messageScrollPane.getVvalue();
            messageScrollPane.setVvalue(vvalue + -deltaY / height);
            event.consume();  // Prevent default scroll behavior
        });

        // Auto-scroll to bottom when new messages arrive
        messageContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            messageScrollPane.setVvalue(1.0);
        });

        // Set up button and input field actions
        sendButton.setOnAction(event -> sendMessage());
        messageField.setOnAction(event -> sendMessage());
        sendImageButton.setOnAction(event -> sendImage());
        serverControlButton.setOnAction(event -> toggleServer());

        // Set initial focus and scroll behavior
        messageScrollPane.requestFocus();
        messageScrollPane.setFocusTraversable(false);
    }

    /**
     * Handles sending a new chat message to all connected clients.
     * Processes the message from the input field and broadcasts it.
     */
    private void sendMessage() {
        String content = messageField.getText().trim();
        if (!content.isEmpty() && chatServer != null && serverRunning) {
            // Process emoji shortcodes and create message
            String enrichedContent = EmojiService.replaceShortcodes(content);
            ChatMessage message = ChatMessage.createTextMessage(
                "Server", 
                AppConfig.SERVER_MESSAGE_COLOR, 
                enrichedContent
            );
            
            // Broadcast to all clients and display locally
            chatServer.broadcastMessage(message);
            displayService.displayText("[Server]: " + enrichedContent, AppConfig.SERVER_MESSAGE_COLOR);
            messageField.clear();
        }
    }

    /**
     * Inserts an emoji at the current caret position in the specified text field.
     *
     * @param field The text field where the emoji should be inserted
     * @param emoji The emoji string to insert
     */
    private void insertEmojiAtCaret(TextField field, String emoji) {
        // Validate inputs
        if (field == null || emoji == null) {
            return;
        }
        
        // Get current text and caret position
        String currentText = field.getText();
        if (currentText == null) {
            currentText = "";
        }
        
        // Insert emoji at caret position
        int caretPosition = Math.max(field.getCaretPosition(), 0);
        StringBuilder builder = new StringBuilder(currentText);
        builder.insert(caretPosition, emoji);
        
        // Update field and reposition caret
        field.setText(builder.toString());
        field.positionCaret(caretPosition + emoji.length());
        field.requestFocus();
    }

    /**
     * Toggles the server state between started and stopped.
     * Delegates to startServer() or stopServer() as needed.
     */
    private void toggleServer() {
        if (!serverRunning) {
            startServer();  // Start if currently stopped
        } else {
            stopServer();   // Stop if currently running
        }
    }

    /**
     * Starts the chat server and enables UI controls.
     * Initializes the ChatServer instance and updates the UI state.
     */
    private void startServer() {
        // Enable input controls
        messageField.setDisable(false);
        sendButton.setDisable(false);
        sendImageButton.setDisable(false);
        if (emojiPickerButton != null) {
            emojiPickerButton.setDisable(false);
        }
        
        // Initialize and start the chat server
        chatServer = new ChatServer(displayService, AppConfig.SERVER_PORT);
        chatServer.start();
        
        // Update state and UI
        serverRunning = true;
        updateServerButton(true);
    }

    /**
     * Stops the chat server and updates the UI state.
     * Cleans up resources and disables input controls.
     */
    private void stopServer() {
        // Shut down the server if running
        if (chatServer != null) {
            chatServer.shutdown();
        }
        
        // Update state and UI
        serverRunning = false;
        updateServerButton(false);
    }

    /**
     * Updates the server control button's appearance based on server state.
     *
     * @param isRunning Whether the server is currently running
     */
    private void updateServerButton(boolean isRunning) {
        if (isRunning) {
            // Update to show stop state
            serverControlButton.setText("Stop Server");
            serverControlButton.setStyle("-fx-background-color: " + AppConfig.SERVER_BUTTON_STOP + ";");
        } else {
            // Update to show start state
            serverControlButton.setText("Start Server");
            serverControlButton.setStyle("-fx-background-color: " + AppConfig.SERVER_BUTTON_START + ";");
        }
    }

    /**
     * Handles sending an image to all connected clients.
     * Opens a file chooser to select an image, encodes it, and broadcasts it.
     */
    private void sendImage() {
        // Verify server is running
        if (!serverRunning || chatServer == null) {
            displayService.displayText("[SERVER] Server not running", AppConfig.ERROR_COLOR);
            return;
        }

        // Open file chooser to select an image
        File selectedFile = FileChooserService.chooseImageFile(null);
        if (selectedFile != null) {
            try {
                // Encode image and create message
                String imageData = ImageService.encodeImage(selectedFile);
                String fileName = selectedFile.getName();
                
                // Create and broadcast image message
                ChatMessage imageMessage = ChatMessage.createImageMessage(
                        "SERVER",
                        AppConfig.SERVER_MESSAGE_COLOR,
                        fileName,
                        imageData
                );
                chatServer.broadcastMessage(imageMessage);
                
                // Display the image locally
                displayService.displayImage(
                        "[SERVER] Image: " + fileName,
                        selectedFile.getAbsolutePath(),
                        AppConfig.SERVER_MESSAGE_COLOR
                );
            } catch (Exception e) {
                // Show error if image processing fails
                displayService.displayText(
                    "[SERVER] Error sending image: " + e.getMessage(), 
                    AppConfig.ERROR_COLOR
                );
            }
        }
    }

    /**
     * Shuts down the server and cleans up resources.
     * Should be called when the application is closing.
     */
    public void shutdown() {
        if (chatServer != null) {
            chatServer.shutdown();
        }
    }
}


