package de.deepamehta.websockets;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



class ConnectionPool {

    // ---------------------------------------------------------------------------------------------- Instance Variables

    /**
     * 1st hash: plugin URI
     * 2nd hash: session ID
     */
    private Map<String, Map<String, WebSocketConnection>> pool = new ConcurrentHashMap();

    // ----------------------------------------------------------------------------------------------------- Constructor

    ConnectionPool() {
    }

    // ----------------------------------------------------------------------------------------- Package Private Methods

    /**
     * Returns the open WebSocket connections associated to the given plugin, or <code>null</code> if there are none.
     */
    Collection<WebSocketConnection> getConnections(String pluginUri) {
        Map connections = pool.get(pluginUri);
        return connections != null ? connections.values() : null;
    }

    void add(WebSocketConnection connection) {
        String pluginUri = connection.pluginUri;
        Map connections = pool.get(pluginUri);
        if (connections == null) {
            connections = new ConcurrentHashMap();
            pool.put(pluginUri, connections);
        }
        connections.put(connection.sessionId, connection);
    }

    void remove(WebSocketConnection connection) {
        String pluginUri = connection.pluginUri;
        boolean removed = getConnections(pluginUri).remove(connection);
        if (!removed) {
            throw new RuntimeException("Removing a connection of plugin \"" + pluginUri + "\" failed");
        }
    }
}
