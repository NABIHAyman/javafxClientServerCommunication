package common.interfaces;

import common.model.ChatMessage;

public interface IMessageSender {
    void sendMessage(ChatMessage message);
    boolean isConnected();
}


