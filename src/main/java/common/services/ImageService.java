package common.services;

import common.config.AppConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Provides utility methods for handling image operations including encoding, decoding, and validation.
 * This service handles the conversion between image files and their Base64 string representations,
 * as well as validating image file extensions.
 * 
 * <p>All methods are static and the class cannot be instantiated.</p>
 */
public class ImageService {
    // Prevent instantiation - all methods are static
    private ImageService() {}

    /**
     * Encodes an image file to a Base64 string.
     *
     * @param file The image file to encode
     * @return A Base64 encoded string representation of the image
     * @throws IOException If an I/O error occurs reading from the file
     * @throws NullPointerException if the file is null
     */
    public static String encodeImage(File file) throws IOException {
        // Read all bytes from the file and encode them to Base64
        byte[] bytes = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Decodes a Base64 string back into an image file and saves it to the system's temporary directory.
     * The resulting file will have a name in the format: "chat_[timestamp]_[originalFileName]".
     *
     * @param base64Data The Base64 encoded image data
     * @param fileName The original file name to use as part of the output filename
     * @return A File object pointing to the decoded image in the temporary directory
     * @throws IOException If an I/O error occurs writing the file
     * @throws IllegalArgumentException if base64Data is null or empty
     * @throws NullPointerException if fileName is null
     */
    public static File decodeImage(String base64Data, String fileName) throws IOException {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            throw new IllegalArgumentException("Base64 data cannot be null or empty");
        }
        
        // Decode the Base64 string to bytes
        byte[] decoded = Base64.getDecoder().decode(base64Data);
        
        // Create a unique filename in the system temp directory
        String tempDir = System.getProperty("java.io.tmpdir");
        File tempFile = new File(tempDir, "chat_" + System.currentTimeMillis() + "_" + fileName);
        
        // Write the decoded bytes to the temporary file
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(decoded);
        }
        
        return tempFile;
    }

    /**
     * Checks if a file has a valid image extension based on the configured extensions in AppConfig.
     * The check is case-insensitive.
     *
     * @param file The file to check
     * @return true if the file has a valid image extension, false otherwise
     * @throws NullPointerException if file is null
     */
    public static boolean isValidImageExtension(File file) {
        String lowerName = file.getName().toLowerCase();
        // Check against all allowed extensions from AppConfig
        for (String ext : AppConfig.IMAGE_EXTENSIONS) {
            // Remove any wildcards and convert to lowercase for comparison
            ext = ext.replace("*", "").toLowerCase();
            if (lowerName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}

