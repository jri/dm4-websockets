package de.deepamehta.websockets;



public interface WebSocketsService {

    void messageToAll(String pluginUri, String message);

    void messageToAllButOne(String pluginUri, String message);

    void messageToOne(String pluginUri, String message);
}
