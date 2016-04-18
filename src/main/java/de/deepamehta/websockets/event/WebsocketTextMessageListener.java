package de.deepamehta.websockets.event;

import de.deepamehta.core.service.EventListener;
import de.deepamehta.websockets.WebSocketConnection;



public interface WebsocketTextMessageListener extends EventListener {

    void websocketTextMessage(String message, WebSocketConnection connection);
}
