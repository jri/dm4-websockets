package de.deepamehta.websockets;



public interface WebSocketsService {

    void broadcast(String pluginUri, String message);

    void broadcast(String pluginUri, String message, WebSocketConnection exclude);
}
