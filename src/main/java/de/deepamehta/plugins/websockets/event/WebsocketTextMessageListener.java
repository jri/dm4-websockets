package de.deepamehta.plugins.websockets.event;

import de.deepamehta.core.service.EventListener;
import de.deepamehta.plugins.websockets.WebSocketConnection;



public interface WebsocketTextMessageListener extends EventListener {

    void websocketTextMessage(String message, WebSocketConnection connection);
}
