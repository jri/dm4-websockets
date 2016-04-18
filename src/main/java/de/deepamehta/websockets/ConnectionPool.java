package de.deepamehta.websockets;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;



class ConnectionPool {

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private Map<String, Queue<WebSocketConnection>> pool = new ConcurrentHashMap();

    // ----------------------------------------------------------------------------------------------------- Constructor

    ConnectionPool() {
    }

    // ----------------------------------------------------------------------------------------- Package Private Methods

    /**
     * Returns the open WebSocket connections associated to the given plugin, or <code>null</code> if there are none.
     */
    Queue<WebSocketConnection> getConnections(String pluginUri) {
        return pool.get(pluginUri);
    }

    void add(WebSocketConnection connection) {
        String pluginUri = connection.pluginUri;
        Queue<WebSocketConnection> connections = getConnections(pluginUri);
        if (connections == null) {
            connections = new ConcurrentLinkedQueue();
            pool.put(pluginUri, connections);
        }
        connections.add(connection);
    }

    void remove(WebSocketConnection connection) {
        String pluginUri = connection.pluginUri;
        boolean removed = getConnections(pluginUri).remove(connection);
        if (!removed) {
            throw new RuntimeException("Removing a connection of plugin \"" + pluginUri + "\" failed");
        }
    }
}
