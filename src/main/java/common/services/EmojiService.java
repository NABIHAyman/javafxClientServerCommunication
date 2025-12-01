package common.services;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides emoji-related functionality including shortcode-to-emoji conversion and emoji image mapping.
 * This service supports a predefined set of emojis that can be referenced by their shortcodes.
 * 
 * <p>Example usage:
 * <pre>
 * String text = "Hello :smile:!";
 * String withEmoji = EmojiService.replaceShortcodes(text);
 * // Result: "Hello 😊!"
 * </pre>
 * 
 * <p>This class is thread-safe and cannot be instantiated.</p>
 */
public final class EmojiService {
    // Map of emoji shortcodes to their Unicode representations
    private static final Map<String, String> EMOJI_MAP;
    
    // Map of emoji shortcodes to their corresponding image filenames
    private static final Map<String, String> EMOJI_IMAGE_MAP;

    // Initialize emoji mappings
    static {
        Map<String, String> emojis = new LinkedHashMap<>();
        emojis.put(":smile:", "\uD83D\uDE0A");
        emojis.put(":laugh:", "\uD83D\uDE02");
        emojis.put(":wink:", "\uD83D\uDE09");
        emojis.put(":thumbs_up:", "\uD83D\uDC4D");
        emojis.put(":thumbs_down:", "\uD83D\uDC4E");
        emojis.put(":heart:", "\u2764");
        emojis.put(":clap:", "\uD83D\uDC4F");
        emojis.put(":fire:", "\uD83D\uDD25");
        emojis.put(":star:", "\uD83D\uDC4A");
        emojis.put(":sunglasses:", "\uD83D\uDE0E");
        EMOJI_MAP = Collections.unmodifiableMap(emojis);

        Map<String, String> images = new LinkedHashMap<>();
        images.put(":smile:", "1f60a.png");
        images.put(":laugh:", "1f602.png");
        images.put(":wink:", "1f609.png");
        images.put(":thumbs_up:", "1f44d.png");
        images.put(":thumbs_down:", "1f44e.png");
        images.put(":heart:", "2764-fe0f.png");
        images.put(":clap:", "1f44f.png");
        images.put(":fire:", "1f525.png");
        images.put(":star:", "1f44a.png");
        images.put(":sunglasses:", "1f60e.png");
        EMOJI_IMAGE_MAP = Collections.unmodifiableMap(images);
    }

    // Prevent instantiation - all methods are static
    private EmojiService() {}

    /**
     * Returns an unmodifiable view of the emoji shortcode-to-unicode map.
     * 
     * @return A map where keys are emoji shortcodes (e.g., ":smile:") and values are their Unicode representations
     */
    public static Map<String, String> getEmojiMap() {
        return EMOJI_MAP;
    }

    /**
     * Returns an unmodifiable view of the emoji shortcode-to-filename map.
     * 
     * @return A map where keys are emoji shortcodes and values are their corresponding image filenames
     */
    public static Map<String, String> getEmojiImageMap() {
        return EMOJI_IMAGE_MAP;
    }

    /**
     * Replaces all emoji shortcodes in the given text with their corresponding Unicode emojis.
     * 
     * @param text The text containing emoji shortcodes (e.g., ":smile:")
     * @return A new string with all recognized emoji shortcodes replaced by their Unicode equivalents,
     *         or the original string if no replacements were made or input is null/empty
     */
    public static String replaceShortcodes(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String result = text;
        // Replace each known emoji shortcode with its Unicode equivalent
        for (Map.Entry<String, String> entry : EMOJI_MAP.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Checks if the given text contains any emoji shortcodes.
     * 
     * @param text The text to check for emoji shortcodes
     * @return true if the text contains at least one emoji shortcode, false otherwise
     */
    public static boolean containsShortcodes(String text) {
        if (text == null) {
            return false;
        }
        // Check if any emoji shortcode exists in the text
        return EMOJI_MAP.keySet().stream().anyMatch(text::contains);
    }

    /**
     * Returns a comma-separated string of all supported emoji shortcodes.
     * This is typically used for displaying help or documentation.
     * 
     * @return A string containing all supported emoji shortcodes, separated by commas
     */
    public static String getSupportedShortcodesDescription() {
        return EMOJI_MAP.keySet().stream().collect(Collectors.joining(", "));
    }
}
