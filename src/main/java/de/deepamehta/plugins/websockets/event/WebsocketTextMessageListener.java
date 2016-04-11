package de.deepamehta.plugins.websockets.event;

import de.deepamehta.core.service.EventListener;

import org.eclipse.jetty.websocket.WebSocket.Connection;



public interface WebsocketTextMessageListener extends EventListener {

    void websocketTextMessage(String message, Connection connection);
}
