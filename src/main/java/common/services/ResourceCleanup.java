package common.services;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Utility class for safely closing resources and handling cleanup operations.
 * This class provides static methods to close various types of resources
 * while suppressing and logging any exceptions that occur during the process.
 * 
 * <p>All methods in this class are null-safe and can be called with null
 * references without throwing exceptions.</p>
 * 
 * <p>Example usage:
 * <pre>
 * // Close multiple closeable resources
 * ResourceCleanup.closeQuietly(inputStream, outputStream);
 * 
 * // Close a socket
 * ResourceCleanup.closeSocket(socket);
 * </pre>
 */
public class ResourceCleanup {
    private static final Logger logger = Logger.getLogger(ResourceCleanup.class.getName());

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private ResourceCleanup() {}

    /**
     * Closes multiple {@link Closeable} resources, suppressing any exceptions.
     * This method is null-safe and handles null elements in the array.
     * 
     * @param resources The resources to close (may be null or contain nulls)
     */
    public static void closeQuietly(Closeable... resources) {
        for (Closeable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (IOException e) {
                    logger.warning("Error closing resource: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Closes a socket connection, suppressing any exceptions.
     * This method is null-safe and checks if the socket is already closed.
     * 
     * @param socket The socket to close (may be null)
     */
    public static void closeSocket(java.net.Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.warning("Error closing socket: " + e.getMessage());
            }
        }
    }
}


