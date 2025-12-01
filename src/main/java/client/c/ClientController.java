package client.c;

// JavaFX core imports
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

// Application configuration and interfaces
import common.config.AppConfig;
import common.interfaces.IDisplayService;
import common.interfaces.IMessageReceiver;
import common.model.ChatMessage;

// Service layer imports
import common.services.FileChooserService;
import common.services.ImageService;
import common.services.EmojiService;
import common.services.MessageDisplayService;
import common.services.MessageFormatter;

// UI component imports
import common.ui.EmojiPicker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

// Java core imports
import java.io.File;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;

// Animation imports
import javafx.scene.paint.Color;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.shape.Circle;

/**
 * Main controller for the chat client interface.
 * Handles user interactions, message sending/receiving, and UI updates.
 * Implements IMessageReceiver to handle incoming messages from the server.
 */
public class ClientController implements IMessageReceiver {
    
    /**
     * Handles the event when a new user connects to the chat.
     * Displays a system message and auto-scrolls to the bottom.
     *
     * @param username The username of the connected user
     */
    @Override
    public void onUserConnected(String username) {
        Platform.runLater(() -> {
            String message = username + " has connected to the chat";
            Label systemMessage = new Label(message);
            systemMessage.getStyleClass().add("system-message");
            messageContainer.getChildren().add(systemMessage);
            messageScrollPane.setVvalue(1.0); // Auto-scroll to bottom
        });
    }

    // UI Components
    @FXML
    private ScrollPane messageScrollPane;  // Scrollable area for chat messages

    @FXML
    private VBox messageContainer;         // Container for all chat messages

    @FXML
    private TextField messageField;        // Input field for new messages

    @FXML
    private Button sendButton;             // Button to send messages

    @FXML
    private Button sendImageButton;        // Button to send images

    @FXML
    private Button clientControlButton;    // Button to connect/disconnect

    @FXML
    private Button emojiPickerButton;      // Button to open emoji picker

    // Authentication overlay components
    @FXML
    private VBox authOverlay;              // Authentication panel overlay
    
    @FXML
    private TextField usernameInput;       // Username input field
    
    @FXML
    private ColorPicker colorPicker;       // Color picker for user messages
    
    @FXML
    private Button overlayCancelButton;    // Cancel authentication button
    
    @FXML
    private Button overlayConnectButton;   // Connect button for authentication

    // Application state
    private ChatClient chatClient;         // Handles network communication with server
    private boolean connected = false;     // Connection status
    private IDisplayService displayService; // Service for displaying messages
    private EmojiPicker emojiPicker;       // Emoji picker component

    // User information
    private String username = "default";   // Current user's username
    private String userColorHex;           // User's chosen message color
    
    // Authentication
    private ContextMenu authPopup;         // Authentication popup menu

    // Typing indicator state
    private final Map<String, TypingIndicator> typingIndicators = new HashMap<>();
    private long lastTypingSentAt = 0L;    // Timestamp of last typing notification
    private static final long TYPING_SEND_INTERVAL_MS = 1500L; // Rate limiting for typing notifications

    /**
     * Initializes the controller after FXML loading is complete.
     * Sets up UI components, event handlers, and initial state.
     */
    @FXML
    public void initialize() {
        // Disable input controls until connected
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

        // Set up button and input field actions
        sendButton.setOnAction(event -> sendMessage());
        messageField.setOnAction(event -> sendMessage());
        sendImageButton.setOnAction(event -> sendImage());
        clientControlButton.setOnAction(event -> toggleConnection());

        // Set up authentication overlay buttons
        if (overlayCancelButton != null) {
            overlayCancelButton.setOnAction(e -> hideOverlay());
        }
        if (overlayConnectButton != null) {
            overlayConnectButton.setOnAction(e -> onOverlayConnect());
        }
        hideOverlay();  // Start with overlay hidden

        // Auto-scroll to bottom when new messages arrive
        messageContainer.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            messageScrollPane.setVvalue(1.0);
        });

        // Handle clicks on scroll pane to manage focus
        messageScrollPane.setOnMouseClicked(event -> {
            if (!messageField.isFocused()) {
                messageScrollPane.requestFocus();
            }
        });

        // Handle clicks on message container to manage focus
        messageContainer.setOnMouseClicked(event -> {
            if (!messageField.isFocused()) {
                messageScrollPane.requestFocus();
            }
        });

        // Handle mouse wheel scrolling
        messageScrollPane.setOnScroll(event -> {
            // Adjust scrolling speed and direction
            double deltaY = event.getDeltaY() * 0.5;
            double width = messageScrollPane.getContent().getBoundsInLocal().getWidth();
            double vValue = messageScrollPane.getVvalue();
            messageScrollPane.setVvalue(vValue + -deltaY / width);
        });

        // Monitor message field for typing activity
        messageField.textProperty().addListener((obs, oldVal, newVal) -> onUserTyping());

        // Handle touchpad scrolling
        messageScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            // Smooth scrolling for touchpad input
            double deltaY = event.getDeltaY();
            double width = messageScrollPane.getContent().getBoundsInLocal().getWidth();
            double height = messageScrollPane.getContent().getBoundsInLocal().getHeight();

            // Calculate and set new scroll position
            double vvalue = messageScrollPane.getVvalue();
            messageScrollPane.setVvalue(vvalue + -deltaY / height);

            event.consume();  // Prevent default scroll behavior
        });

        // Configure scroll pane focus behavior
        messageScrollPane.setFocusTraversable(false);
    }

    /**
     * Handles user typing activity in the message field.
     * Sends typing indicators to other users with rate limiting.
     */
    private void onUserTyping() {
        // Only proceed if client is connected
        if (chatClient == null || !chatClient.isConnected()) return;
        
        // Get current text and handle null case
        String text = messageField.getText();
        if (text == null) text = "";
        
        // Hide typing indicator if field is empty
        if (text.isBlank()) {
            hideTypingIndicator(username);
            return;
        }
        
        // Rate limit typing notifications to avoid flooding
        long now = System.currentTimeMillis();
        if (now - lastTypingSentAt >= TYPING_SEND_INTERVAL_MS) {
            lastTypingSentAt = now;
            ChatMessage typing = ChatMessage.createTextMessage(username, userColorHex, "[TYPING]");
            chatClient.sendMessage(typing);
        }
    }

    /**
     * Displays a typing indicator for the specified user.
     * Creates a new indicator if one doesn't exist, or updates the existing one.
     *
     * @param sender The username of the typing user
     * @param color The color to use for the indicator
     */
    private void showTypingIndicator(String sender, String color) {
        TypingIndicator indicator = typingIndicators.get(sender);
        if (indicator == null) {
            // Create and add new typing indicator
            indicator = new TypingIndicator(sender, color);
            typingIndicators.put(sender, indicator);
            messageContainer.getChildren().add(indicator.node);
        }
        // Update the last activity timestamp
        indicator.touch();
    }

    /**
     * Hides and removes the typing indicator for the specified user.
     *
     * @param sender The username of the user who stopped typing
     */
    private void hideTypingIndicator(String sender) {
        TypingIndicator indicator = typingIndicators.remove(sender);
        if (indicator != null) {
            indicator.stop();
            messageContainer.getChildren().remove(indicator.node);
        }
    }

    private class TypingIndicator {
        final HBox node = new HBox(6);
        final Timeline timeline = new Timeline();
        long lastTouched = System.currentTimeMillis();
        final String owner;

        TypingIndicator(String owner, String color) {
            this.owner = owner;
            node.getStyleClass().add("typing-indicator");

            Label label = new Label(owner + " is typing");
            label.setStyle("-fx-text-fill: " + (color != null ? color : AppConfig.CLIENT_MESSAGE_COLOR) + "; -fx-opacity: 0.9;");

            HBox dots = new HBox(4);
            dots.getStyleClass().add("typing-dots");
            Circle d1 = buildDot();
            Circle d2 = buildDot();
            Circle d3 = buildDot();
            dots.getChildren().addAll(d1, d2, d3);

            node.getChildren().addAll(label, dots);
            node.setAlignment(Pos.BOTTOM_LEFT);

            animateDots(d1, d2, d3);
            scheduleAutoHide();
        }

        private Circle buildDot() {
            Circle c = new Circle(2.5);
            c.getStyleClass().add("typing-dot");
            c.setOpacity(0.25);
            return c;
        }

        private void animateDots(Circle d1, Circle d2, Circle d3) {
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.getKeyFrames().setAll(
                    new KeyFrame(Duration.ZERO, e -> setDotOpacities(d1, d2, d3, 1.0, 0.25, 0.25)),
                    new KeyFrame(Duration.millis(300), e -> setDotOpacities(d1, d2, d3, 0.25, 1.0, 0.25)),
                    new KeyFrame(Duration.millis(600), e -> setDotOpacities(d1, d2, d3, 0.25, 0.25, 1.0)),
                    new KeyFrame(Duration.millis(900), e -> setDotOpacities(d1, d2, d3, 0.25, 0.25, 0.25))
            );
            timeline.play();
        }

        private void setDotOpacities(Circle d1, Circle d2, Circle d3, double o1, double o2, double o3) {
            d1.setOpacity(o1);
            d2.setOpacity(o2);
            d3.setOpacity(o3);
        }

        void touch() {
            lastTouched = System.currentTimeMillis();
        }

        void scheduleAutoHide() {
            Timeline autoHide = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
                if (System.currentTimeMillis() - lastTouched >= 2500) {
                    hideTypingIndicator(owner);
                } else {
                    scheduleAutoHide();
                }
            }));
            autoHide.setCycleCount(1);
            autoHide.play();
        }

        void stop() {
            timeline.stop();
        }
    }

    /**
     * Gets the display service instance used for showing messages.
     *
     * @return The IDisplayService instance
     */
    private IDisplayService getDisplayService() {
        return displayService;
    }

    private void sendMessage() {
        String content = messageField.getText().trim();
        if (!content.isEmpty() && chatClient != null && chatClient.isConnected()) {
            String enrichedContent = EmojiService.replaceShortcodes(content);
            ChatMessage message = ChatMessage.createTextMessage(username, userColorHex, enrichedContent);
            chatClient.sendMessage(message);
            messageField.clear();

            hideTypingIndicator(username);
        }
    }

    private void insertEmojiAtCaret(TextField field, String emoji) {
        if (field == null || emoji == null) {
            return;
        }

        field.setDisable(true);

        try {
            String currentText = field.getText();
            int caretPosition = field.getCaretPosition();

            String newText = new StringBuilder(currentText)
                .insert(caretPosition, emoji)
                .toString();

            int newCaretPosition = caretPosition + emoji.length();

            boolean wasFocused = field.isFocused();

            field.deselect();

            field.setText(newText);

            field.positionCaret(newCaretPosition);

            if (wasFocused) {
                Platform.runLater(field::requestFocus);
            }
        } finally {
            field.setDisable(false);
        }
    }

    private void toggleConnection() {
        if (!connected) {
            showAuthPopup(clientControlButton);
        } else {
            disconnect();
        }
    }

    private void disconnect() {
        if (chatClient != null) {
            new Thread(() -> {
                chatClient.shutdown();
                Platform.runLater(() -> {
                    connected = false;
                    updateConnectionButton(false);
                });
            }).start();
        }
    }

    /**
     * Updates the connection button's state and appearance based on connection status.
     * Enables/disables input controls and changes button text/color.
     *
     * @param isConnected Whether the client is currently connected to the server
     */
    private void updateConnectionButton(boolean isConnected) {
        if (isConnected) {
            // Enable input controls when connected
            messageField.setDisable(false);
            sendButton.setDisable(false);
            sendImageButton.setDisable(false);
            if (emojiPickerButton != null) {
                emojiPickerButton.setDisable(false);
            }
            // Update button to show disconnect option
            clientControlButton.setText("Disconnect");
            clientControlButton.setStyle("-fx-background-color: " + AppConfig.SERVER_BUTTON_STOP + ";");
        } else {
            // Disable input controls when disconnected
            messageField.setDisable(true);
            sendButton.setDisable(true);
            sendImageButton.setDisable(true);
            if (emojiPickerButton != null) {
                emojiPickerButton.setDisable(true);
            }
            // Update button to show connect option
            clientControlButton.setText("Connect");
            clientControlButton.setStyle("-fx-background-color: " + AppConfig.SERVER_BUTTON_START + ";");
        }
    }

    private void sendImage() {
        if (!connected || chatClient == null || !chatClient.isConnected()) {
            getDisplayService().displayText("[CLIENT] Not connected", AppConfig.ERROR_COLOR);
            return;
        }

        File selectedFile = FileChooserService.chooseImageFile(null);
        if (selectedFile != null) {
            try {
                String imageData = ImageService.encodeImage(selectedFile);
                String fileName = selectedFile.getName();
                ChatMessage imageMessage = ChatMessage.createImageMessage(
                        username,
                        userColorHex,
                        fileName,
                        imageData
                );
                chatClient.sendMessage(imageMessage);
            } catch (Exception e) {
                getDisplayService().displayText("[CLIENT] Error sending image: " + e.getMessage(), AppConfig.ERROR_COLOR);
            }
        }
    }

    @Override
    public void onMessageReceived(ChatMessage message) {
        IDisplayService service = getDisplayService();

        if (message.isTextMessage() && "[TYPING]".equals(message.getContent())) {
            if (message.getSender() != null && !message.getSender().equals(username)) {
                showTypingIndicator(message.getSender(), message.getColor() != null ? message.getColor() : AppConfig.CLIENT_MESSAGE_COLOR);
            }
            return;
        }

        if (message.isImageMessage()) {
            handleImageMessage(message, service);
        } else {
            handleTextMessage(message, service);
        }
    }

    private void handleImageMessage(ChatMessage message, IDisplayService service) {
        try {
            File imageFile = ImageService.decodeImage(message.getImageData(), message.getFileName());
            String label = MessageFormatter.formatForDisplay(message);
            String color = message.getColor() != null ? message.getColor() : AppConfig.CLIENT_MESSAGE_COLOR;
            service.displayImage(label, imageFile.getAbsolutePath(), color);
        } catch (Exception e) {
            service.displayText("[CLIENT] Failed to load image: " + e.getMessage(), AppConfig.ERROR_COLOR);
        }
    }

    private void handleTextMessage(ChatMessage message, IDisplayService service) {
        String displayText = MessageFormatter.formatForDisplay(message);
        String color = message.getColor() != null ? message.getColor() : AppConfig.CLIENT_MESSAGE_COLOR;
        if (message.getSender() != null) {
            hideTypingIndicator(message.getSender());
        }
        service.displayText(displayText, color);
    }

    /**
     * Handles error messages from the chat client.
     * Displays the error and updates the connection state.
     *
     * @param error The error message to display
     */
    @Override
    public void onError(String error) {
        getDisplayService().displayText(error, AppConfig.ERROR_COLOR);
        connected = false;
        updateConnectionButton(false);
    }

    /**
     * Handles the connect action from the authentication overlay.
     * Initializes the chat client with user credentials and connects to the server.
     */
    private void onOverlayConnect() {
        // Get and validate username
        String proposedName = usernameInput != null ? usernameInput.getText() : null;
        this.username = (proposedName == null || proposedName.isBlank()) ? "default" : proposedName.trim();
        
        // Get and convert selected color
        Color picked = colorPicker != null ? colorPicker.getValue() : null;
        this.userColorHex = picked != null ? colorToHex(picked) : AppConfig.CLIENT_MESSAGE_COLOR;

        // Initialize and start chat client
        chatClient = new ChatClient(this, "localhost", AppConfig.SERVER_PORT);

        // Send join message if credentials are valid
        if (username != null && userColorHex != null) {
            ChatMessage join = ChatMessage.createTextMessage(username, userColorHex, "[JOIN]");
            chatClient.setInitialHandshake(join);
        }
        
        // Start client and update UI
        chatClient.start();
        connected = true;
        updateConnectionButton(true);
        hideOverlay();
    }

    /**
     * Shows the authentication overlay with current user settings.
     * Initializes form fields and sets up keyboard navigation.
     */
    private void showOverlay() {
        if (authOverlay != null) {
            // Make overlay visible
            authOverlay.setVisible(true);
            authOverlay.setManaged(true);
            
            // Initialize color picker with current color
            if (colorPicker != null) {
                try {
                    Color initial = (userColorHex != null) ? Color.web(userColorHex) : Color.web(AppConfig.CLIENT_MESSAGE_COLOR);
                    colorPicker.setValue(initial);
                } catch (Exception ignored) {
                    // Ignore color parsing errors
                }
            }
            
            // Initialize username field
            if (usernameInput != null) {
                if (username != null && !username.isBlank()) {
                    usernameInput.setText(username);
                    usernameInput.positionCaret(usernameInput.getText().length());
                }
                usernameInput.requestFocus();
            }
            
            // Set up keyboard navigation
            authOverlay.setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case ESCAPE -> hideOverlay();  // Close on ESC
                    case ENTER -> onOverlayConnect();  // Connect on Enter
                    default -> {}
                }
            });
            
            // Ensure overlay has focus
            authOverlay.requestFocus();
        }
    }

    /**
     * Hides the authentication overlay.
     * Removes the overlay from layout and visibility.
     */
    private void hideOverlay() {
        if (authOverlay != null) {
            authOverlay.setVisible(false);
            authOverlay.setManaged(false);
        }
    }

    /**
     * Validates if a string is a valid hex color code.
     *
     * @param value The color string to validate (e.g., "#FF0000")
     * @return true if the string is a valid hex color, false otherwise
     */
    private boolean validHexColor(String value) {
        if (value == null) return false;
        return Pattern.compile("^#([A-Fa-f0-9]{6})$").matcher(value.trim()).matches();
    }

    /**
     * Converts a JavaFX Color object to a hex color string.
     *
     * @param c The JavaFX Color to convert
     * @return The hex color string (e.g., "#FF0000" for red)
     */
    private String colorToHex(Color c) {
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private void showAuthPopup(Node owner) {
        if (authPopup == null) {
            authPopup = buildAuthPopup();
        }
        if (authPopup.isShowing()) {
            authPopup.hide();
            return;
        }
        authPopup.show(owner, 0, 0);
        Window window = owner.getScene().getWindow();
        double centerX = window.getX() + (window.getWidth() / 2) - (authPopup.getWidth() / 2);
        double centerY = window.getY() + (window.getHeight() / 2) - (authPopup.getHeight() / 2);
        Screen screen = Screen.getScreensForRectangle(window.getX(), window.getY(), window.getWidth(), window.getHeight()).get(0);
        Rectangle2D bounds = screen.getVisualBounds();
        centerX = Math.max(bounds.getMinX(), Math.min(centerX, bounds.getMaxX() - authPopup.getWidth()));
        centerY = Math.max(bounds.getMinY(), Math.min(centerY, bounds.getMaxY() - authPopup.getHeight()));
        authPopup.hide();
        authPopup.show(owner, centerX, centerY);
    }

    private ContextMenu buildAuthPopup() {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("emoji-picker");

        VBox card = new VBox();
        card.getStyleClass().add("auth-card");
        card.setSpacing(10);

        Label title = new Label("Connect");
        title.getStyleClass().add("auth-title");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter username");
        if (username != null && !username.isBlank()) {
            nameField.setText(username);
            nameField.positionCaret(username.length());
        }

        ColorPicker picker = new ColorPicker();
        try {
            picker.setValue(userColorHex != null ? Color.web(userColorHex) : Color.web(AppConfig.CLIENT_MESSAGE_COLOR));
        } catch (Exception ignored) {}

        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Button cancel = new Button("Cancel");
        Button connectBtn = new Button("Connect");
        connectBtn.setDefaultButton(true);
        actions.getChildren().addAll(cancel, connectBtn);

        card.getChildren().addAll(title, nameField, picker, actions);

        CustomMenuItem item = new CustomMenuItem(card);
        item.setHideOnClick(false);
        menu.getItems().add(item);

        cancel.setOnAction(e -> menu.hide());
        connectBtn.setOnAction(e -> {
            String proposedName = nameField.getText();
            username = (proposedName == null || proposedName.isBlank()) ? "default" : proposedName.trim();
            userColorHex = colorToHex(picker.getValue());
            chatClient = new ChatClient(this, "localhost", AppConfig.SERVER_PORT);
            if (username != null && userColorHex != null) {
                ChatMessage join = ChatMessage.createTextMessage(username, userColorHex, "[JOIN]");
                chatClient.setInitialHandshake(join);
            }
            chatClient.start();
            connected = true;
            updateConnectionButton(true);
            menu.hide();
        });

        card.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> menu.hide();
                case ENTER -> connectBtn.fire();
                default -> {}
            }
        });
        menu.setOnShown(e -> nameField.requestFocus());
        return menu;
    }

    /**
     * Shuts down the client and cleans up resources.
     * Called when the application is closing.
     */
    public void shutdown() {
        disconnect();
    }
}
