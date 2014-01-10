package de.deepamehta.plugins.websockets;

import de.deepamehta.plugins.websockets.event.WebsocketTextMessageListener;
import de.deepamehta.plugins.websockets.service.WebSocketsService;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.DeepaMehtaEvent;
import de.deepamehta.core.service.Listener;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.TypeUtil;
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



public class WebSocketsPlugin extends PluginActivator implements WebSocketsService {

    // ------------------------------------------------------------------------------------------------------- Constants

    private static final int WEBSOCKETS_PORT = 8081;

    // Events
    private static DeepaMehtaEvent WEBSOCKET_TEXT_MESSAGE = new DeepaMehtaEvent(WebsocketTextMessageListener.class) {
        @Override
        public void deliver(Listener listener, Object... params) {
            ((WebsocketTextMessageListener) listener).websocketTextMessage(
                (String) params[0]
            );
        }
    };
    // ### TODO: define further events, OPEN, CLOSE, BINARY_MESSAGE, ...

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private WebSocketServer server;

    private Logger logger = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods

    // *** WebSocketsService Implementation ***

    @Override
    public void broadcast(String pluginUri, String message) {
        server.broadcast(pluginUri, message);
    }

    // *** Hook Implementations ***

    @Override
    public void init() {
        try {
            logger.info("##### Starting Jetty WebSocket server #####");
            server = new WebSocketServer(WEBSOCKETS_PORT);
            server.start();
            // ### server.join();
            logger.info("### Jetty WebSocket server started successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Starting Jetty WebSocket server failed");
        }
    }

    @Override
    public void shutdown() {
        try {
            logger.info("##### Stopping Jetty WebSocket server #####");
            server.stop();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Stopping Jetty WebSocket server failed");
        }
    }

    // ------------------------------------------------------------------------------------------------- Private Classes

    private class WebSocketServer extends Server {

        private Map<String, Queue<Connection>> pluginConnections = new ConcurrentHashMap();

        private WebSocketServer(int port) {
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

        // ---

        private void broadcast(String pluginUri, String message) {
            for (Connection connection : getConnections(pluginUri)) {
                try {
                    connection.sendMessage(message);
                } catch (Exception e) {
                    removeConnection(pluginUri, connection);
                    logger.log(Level.SEVERE, "Sending message via " + connection + " failed -- connection removed", e);
                }
            }
        }

        // ---

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
                throw new RuntimeException("Removing a connection for plugin \"" + pluginUri + "\" failed");
            }
        }

        // ---

        private class PluginWebSocket implements WebSocket, WebSocket.OnTextMessage, WebSocket.OnBinaryMessage,
                                                            WebSocket.OnFrame, WebSocket.OnControl {
            private String pluginUri;
            private Connection connection;

            private PluginWebSocket(String pluginUri) {
                try {
                    if (pluginUri == null) {
                        throw new RuntimeException("Missing plugin URI -- Put the URI of your plugin " +
                            "in the JavaScript WebSocket constructor (2nd parameter)");
                    } else {
                        dms.getPlugin(pluginUri);   // check plugin URI, throws if invalid
                    }
                    //
                    logger.info("### Opening a WebSocket connection for plugin \"" + pluginUri + "\"");
                    this.pluginUri = pluginUri;
                } catch (Exception e) {
                    throw new RuntimeException("Opening WebSocket connection failed", e);
                }
            }

            // *** WebSocket ***

            @Override
            public void onOpen(Connection connection) {
                System.err.printf("PluginWebSocket#onOpen      %s\n", connection);
                addConnection(pluginUri, connection);
            }

            @Override
            public void onClose(int code, String message) {
                System.err.printf("PluginWebSocket#onClose     %d %s\n", code, message);
                removeConnection(pluginUri, connection);
            }

            // *** WebSocket.OnTextMessage ***

            @Override
            public void onMessage(String message) {
                System.err.printf("PluginWebSocket#onMessage   %s\n", message);
                dms.deliverEvent(pluginUri, WEBSOCKET_TEXT_MESSAGE, message);
            }

            // *** WebSocket.OnBinaryMessage ***

            @Override
            public void onMessage(byte[] data, int offset, int length) {
                System.err.printf("PluginWebSocket#onMessage   %s\n", TypeUtil.toHexString(data, offset, length));
            }

            // *** WebSocket.OnFrame ***

            @Override
            public boolean onFrame(byte flags, byte opcode, byte[] data, int offset, int length) {            
                System.err.printf("PluginWebSocket#onFrame     %s|%s %s\n", TypeUtil.toHexString(flags),
                    TypeUtil.toHexString(opcode), TypeUtil.toHexString(data, offset, length));
                return false;
            }
        
            @Override
            public void onHandshake(FrameConnection connection) {
                System.err.printf("PluginWebSocket#onHandshake %s\n", connection);
                this.connection = connection;
            }

            // *** WebSocket.OnControl ***

            @Override
            public boolean onControl(byte controlCode, byte[] data, int offset, int length) {
                System.err.printf("PluginWebSocket#onControl   %s %s\n", TypeUtil.toHexString(controlCode),
                    TypeUtil.toHexString(data, offset, length));            
                return false;
            }
        }
    }
}
