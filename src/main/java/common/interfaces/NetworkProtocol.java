package common.interfaces;

import common.model.ChatMessage;
import java.io.IOException;

public interface NetworkProtocol {
    void sendMessage(ChatMessage message) throws IOException;
    String receiveMessage() throws IOException;
    void close();
    boolean isConnected();
}
