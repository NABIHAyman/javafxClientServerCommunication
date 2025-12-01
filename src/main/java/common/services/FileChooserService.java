package common.services;

import common.config.AppConfig;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

/**
 * Provides file chooser functionality for selecting image files.
 * This service handles the creation and configuration of file chooser dialogs
 * with appropriate file filters based on supported image formats.
 * 
 * <p>Example usage:
 * <pre>
 * File selectedFile = FileChooserService.chooseImageFile(primaryStage);
 * if (selectedFile != null) {
 *     // Handle the selected file
 * }
 * </pre>
 * 
 * <p>This class cannot be instantiated and contains only static methods.</p>
 */
public class FileChooserService {

    // Prevent instantiation - all methods are static
    private FileChooserService() {}

    /**
     * Displays a file chooser dialog for selecting an image file.
     * The dialog is configured to show only the image file types specified in AppConfig.IMAGE_EXTENSIONS.
     *
     * @param ownerWindow The parent window for the file chooser dialog, or null for no owner
     * @return The selected File object, or null if no file was selected or the dialog was canceled
     * @see common.config.AppConfig#IMAGE_EXTENSIONS
     */
    public static File chooseImageFile(Window ownerWindow) {
        // Create and configure the file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an image");
        
        // Set up file extension filter using supported image types from AppConfig
        String extensions = String.join(";", AppConfig.IMAGE_EXTENSIONS);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", extensions)
        );

        // Show the file chooser dialog and return the selected file
        return fileChooser.showOpenDialog(ownerWindow);
    }
}
