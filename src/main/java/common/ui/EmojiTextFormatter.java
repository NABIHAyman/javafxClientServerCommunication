package common.ui;

// Service imports
import common.services.EmojiService;

// JavaFX UI components
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Handles the formatting and display of text with emoji support.
 * Converts emoji shortcodes to actual emoji characters and applies
 * appropriate styling to ensure proper rendering in the chat interface.
 */
public class EmojiTextFormatter {
    /**
     * Creates a TextFlow containing formatted text with emoji support.
     * Processes the input text to replace emoji shortcodes and applies
     * appropriate styling to both text and emoji characters.
     *
     * @param text The input text potentially containing emoji shortcodes
     * @return TextFlow containing formatted text with emoji support
     */
    public static TextFlow createFormattedText(String text) {
        // Create a new TextFlow container for the formatted text
        TextFlow textFlow = new TextFlow();
        textFlow.getStyleClass().add("message-text");

        // Return empty TextFlow for null or empty input
        if (text == null || text.isEmpty()) {
            return textFlow;
        }

        // Replace emoji shortcodes with actual emoji characters
        String normalized = EmojiService.replaceShortcodes(text);

        // Buffer for accumulating regular text between emojis
        StringBuilder buffer = new StringBuilder();
        int i = 0;
        final int length = normalized.length();

        // Process each character in the normalized string
        while (i < length) {
            int cp = normalized.codePointAt(i);
            
            // Check if current code point is an emoji
            if (isEmoji(cp)) {
                // If we have buffered text, add it as a text node first
                if (buffer.length() > 0) {
                    Text t = createTextNode(buffer.toString());
                    t.getStyleClass().add("message-text");
                    textFlow.getChildren().add(t);
                    buffer.setLength(0); // Clear the buffer
                }

                // Create and style the emoji text node
                int charCount = Character.charCount(cp);
                String emojiStr = normalized.substring(i, i + charCount);
                Text emojiText = new Text(emojiStr);
                emojiText.getStyleClass().add("emoji");
                // Apply emoji-specific styling with fallback fonts
                emojiText.setStyle(
                    "-fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji', 'Apple Color Emoji', 'Android Emoji', 'EmojiSymbols', 'Symbola'; " +
                    "-fx-font-size: 56px;"
                );
                textFlow.getChildren().add(emojiText);
                i += charCount;
            } else {
                // Accumulate regular text in the buffer
                buffer.appendCodePoint(cp);
                i += Character.charCount(cp);
            }
        }

        // Add any remaining text in the buffer
        if (buffer.length() > 0) {
            Text t = createTextNode(buffer.toString());
            t.getStyleClass().add("message-text");
            textFlow.getChildren().add(t);
        }

        return textFlow;
    }

    /**
     * Creates a styled Text node with the specified text content.
     * Applies standard text styling for consistent appearance in the chat.
     *
     * @param text The text content for the node
     * @return A styled Text node
     */
    private static Text createTextNode(String text) {
        Text node = new Text(text);
        // Apply standard text styling with system font fallbacks
        node.setStyle(
            "-fx-font-family: 'Segoe UI', Arial, sans-serif; " +
            "-fx-font-size: 14px;"
        );
        return node;
    }

    /**
     * Determines if a Unicode code point represents an emoji character.
     * Checks against known emoji code point ranges.
     *
     * @param codePoint The Unicode code point to check
     * @return true if the code point is an emoji, false otherwise
     */
    private static boolean isEmoji(int codePoint) {
        // Check against various Unicode blocks that contain emoji characters
        return (codePoint >= 0x1F300 && codePoint <= 0x1FAFF) || // Emoticons, Misc Symbols, and Pictographs
               (codePoint >= 0x1F600 && codePoint <= 0x1F64F) || // Emoticons (Emoji)
               (codePoint >= 0x2600 && codePoint <= 0x26FF)   || // Misc symbols
               (codePoint >= 0x2700 && codePoint <= 0x27BF)   || // Dingbats
               (codePoint == 0x2764);                            // Heart symbol
    }

    /**
     * Checks if a Unicode code point represents a skin tone modifier.
     * Skin tone modifiers are used to change the appearance of emoji characters.
     *
     * @param codePoint The Unicode code point to check
     * @return true if the code point is a skin tone modifier, false otherwise
     */
    private static boolean isSkinTone(int codePoint) {
        // Skin tone modifiers range from U+1F3FB to U+1F3FF
        return codePoint >= 0x1F3FB && codePoint <= 0x1F3FF;
    }
}