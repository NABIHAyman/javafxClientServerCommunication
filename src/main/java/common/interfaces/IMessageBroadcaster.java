package common.interfaces;

import common.model.ChatMessage;

public interface IMessageBroadcaster {
    void broadcast(ChatMessage message);
    void broadcastToAll(ChatMessage message);
}


