package de.deepamehta.plugins.websockets;

import de.deepamehta.plugins.websockets.event.WebsocketTextMessageListener;
import de.deepamehta.core.service.CoreService;
import de.deepamehta.core.service.DeepaMehtaEvent;
import de.deepamehta.core.service.EventListener;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketHandler;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;



class WebSocketsServer extends Server {

    // ------------------------------------------------------------------------------------------------------- Constants

    // Events
    private static DeepaMehtaEvent WEBSOCKET_TEXT_MESSAGE = new DeepaMehtaEvent(WebsocketTextMessageListener.class) {
        @Override
        public void dispatch(EventListener listener, Object... params) {
            ((WebsocketTextMessageListener) listener).websocketTextMessage(
                (String) params[0], (Connection) params[1]
            );
        }
    };
    // ### TODO: define further events, OPEN, CLOSE, BINARY_MESSAGE, ...

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private Map<String, Queue<Connection>> pluginConnections = new ConcurrentHashMap();

    private CoreService dm4;

    private Logger logger = Logger.getLogger(getClass().getName());

    // ----------------------------------------------------------------------------------------------------- Constructor

    WebSocketsServer(int port, CoreService dm4) {
        this.dm4 = dm4;
        //
        // add connector
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        addConnector(connector);
        //
        // set WebSocket handler
        setHandler(new WebSocketHandler() {
            @Override
            public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
                return new PluginWebSocket(protocol);
            }
        });
    }

    // ----------------------------------------------------------------------------------------- Package Private Methods

    void broadcast(String pluginUri, String message, Connection exclude) {
        Queue<Connection> connections = getConnections(pluginUri);
        if (connections != null) {
            for (Connection connection : connections) {
                try {
                    if (connection != exclude) {
                        connection.sendMessage(message);
                    }
                } catch (Exception e) {
                    removeConnection(pluginUri, connection);
                    logger.log(Level.SEVERE, "Sending message via " + connection + " failed -- connection removed", e);
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------- Private Methods

    /**
     * Returns the open WebSocket connections associated to the given plugin, or <code>null</code> if there are none.
     */
    private Queue<Connection> getConnections(String pluginUri) {
        return pluginConnections.get(pluginUri);
    }

    private void addConnection(String pluginUri, Connection connection) {
        Queue<Connection> connections = getConnections(pluginUri);
        if (connections == null) {
            connections = new ConcurrentLinkedQueue<Connection>();
            pluginConnections.put(pluginUri, connections);
        }
        connections.add(connection);
    }

    private void removeConnection(String pluginUri, Connection connection) {
        boolean removed = getConnections(pluginUri).remove(connection);
        if (!removed) {
            throw new RuntimeException("Removing a connection of plugin \"" + pluginUri + "\" failed");
        }
    }

    // ------------------------------------------------------------------------------------------------- Private Classes

    /**
     * A WebSocket connection that is bound to a DeepaMehta plugin.
     * When a message arrives on the connection the plugin is notified via a WEBSOCKET_TEXT_MESSAGE event.
     */
    private class PluginWebSocket implements WebSocket, WebSocket.OnTextMessage, WebSocket.OnBinaryMessage {

        private String pluginUri;
        private Connection connection;

        private PluginWebSocket(String pluginUri) {
            this.pluginUri = pluginUri;
            try {
                if (pluginUri == null) {
                    throw new RuntimeException("Missing plugin URI -- Add your plugin's URI " +
                        "as the 2nd argument to the JavaScript WebSocket constructor");
                } else {
                    dm4.getPlugin(pluginUri);   // check plugin URI, throws if invalid
                }
            } catch (Exception e) {
                throw new RuntimeException("Opening WebSocket connection failed", e);
            }
        }

        // *** WebSocket ***

        @Override
        public void onOpen(Connection connection) {
            logger.info("### Opening a WebSocket connection for plugin \"" + pluginUri + "\"");
            this.connection = connection;
            addConnection(pluginUri, connection);
        }

        @Override
        public void onClose(int code, String message) {
            logger.info("### Closing a WebSocket connection of plugin \"" + pluginUri + "\"");
            removeConnection(pluginUri, connection);
        }

        // *** WebSocket.OnTextMessage ***

        @Override
        public void onMessage(String message) {
            try {
                dm4.dispatchEvent(pluginUri, WEBSOCKET_TEXT_MESSAGE, message, connection);
            } catch (Exception e) {
                // Note: we don't rethrow to Jetty here. It would not log the exception's cause. DM's exception
                // mapper would not kick in either as the plugin is called from Jetty directly, not Jersey.
                logger.log(Level.SEVERE, "An error ocurred while processing a WebSocket message:", e);
            }
        }

        // *** WebSocket.OnBinaryMessage ***

        @Override
        public void onMessage(byte[] data, int offset, int length) {
            // ### TODO
        }
    }
}
