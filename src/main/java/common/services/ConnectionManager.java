package common.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Manages a socket connection, providing simplified methods for reading and writing messages.
 * This class encapsulates the low-level socket I/O operations and provides a cleaner
 * interface for sending and receiving text messages over a network connection.
 * 
 * <p>This class is thread-safe for individual method calls, but concurrent access to
 * the same instance from multiple threads should be synchronized externally if needed.</p>
 * 
 * <p>Example usage:
 * <pre>
 * try (Socket socket = new Socket("localhost", 12345)) {
 *     ConnectionManager manager = new ConnectionManager(socket);
 *     manager.sendMessage("Hello, server!");
 *     String response = manager.readLine();
 * } catch (IOException e) {
 *     // Handle exception
 * }
 * </pre>
 * 
 * @see java.net.Socket
 * @see ResourceCleanup
 */
public class ConnectionManager {
    /** The underlying socket connection */
    private final Socket socket;
    
    /** Reader for incoming messages */
    private BufferedReader input;
    
    /** Writer for outgoing messages */
    private PrintWriter output;

    /**
     * Creates a new ConnectionManager for the specified socket.
     * Initializes the input and output streams for the connection.
     *
     * @param socket The socket to manage
     * @throws IOException if an I/O error occurs when creating the input/output streams
     * @throws IllegalArgumentException if socket is null or already closed
     */
    public ConnectionManager(Socket socket) throws IOException {
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * Reads a line of text from the connection.
     * This method blocks until a complete line is received or the end of the stream is reached.
     *
     * @return A String containing the contents of the line, or null if the end of the stream has been reached
     * @throws IOException if an I/O error occurs or if the connection is closed
     * @throws IllegalStateException if the connection is not properly initialized
     */
    public String readLine() throws IOException {
        return input.readLine();
    }

    /**
     * Sends a message over the connection.
     * The message is automatically followed by a newline character.
     *
     * @param message The message to send (may be null, which will send an empty line)
     * @throws IllegalStateException if the connection is not properly initialized
     */
    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }

    /**
     * Checks if the connection is currently active.
     * 
     * @return true if the socket is not null, not closed, and connected; false otherwise
     */
    public boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    /**
     * Closes the connection and releases all associated resources.
     * This method is idempotent and can be called multiple times safely.
     * It ensures that all I/O streams and the underlying socket are properly closed.
     */
    public void close() {
        ResourceCleanup.closeQuietly(input, output);
        ResourceCleanup.closeSocket(socket);
    }

    /**
     * Returns the underlying socket.
     * 
     * @return The Socket object being managed by this ConnectionManager
     */
    public Socket getSocket() {
        return this.socket;
    }
}


