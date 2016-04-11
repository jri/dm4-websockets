package de.deepamehta.plugins.websockets;

import org.eclipse.jetty.websocket.WebSocket.Connection;



public interface WebSocketsService {

    void broadcast(String pluginUri, String message);

    void broadcast(String pluginUri, String message, Connection exclude);
}
