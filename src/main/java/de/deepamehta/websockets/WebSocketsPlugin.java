package de.deepamehta.websockets;

import de.deepamehta.websockets.event.WebsocketTextMessageListener;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.DeepaMehtaEvent;
import de.deepamehta.core.service.EventListener;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

import javax.servlet.http.HttpServletRequest;

import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;



public class WebSocketsPlugin extends PluginActivator implements WebSocketsService {

    // ------------------------------------------------------------------------------------------------------- Constants

    private static final int WEBSOCKETS_PORT = 8081;

    // Events
    static DeepaMehtaEvent WEBSOCKET_TEXT_MESSAGE = new DeepaMehtaEvent(WebsocketTextMessageListener.class) {
        @Override
        public void dispatch(EventListener listener, Object... params) {
            ((WebsocketTextMessageListener) listener).websocketTextMessage(
                (String) params[0], (WebSocketConnection) params[1]
            );
        }
    };

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private WebSocketsServer server;
    private ConnectionPool pool = new ConnectionPool();

    private Logger logger = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods

    // *** WebSocketsService Implementation ***

    @Override
    public void broadcast(String pluginUri, String message) {
        broadcast(pluginUri, message, null);    // exclude=null
    }

    @Override
    public void broadcast(String pluginUri, String message, WebSocketConnection exclude) {
        Queue<WebSocketConnection> connections = pool.getConnections(pluginUri);
        if (connections != null) {
            for (WebSocketConnection connection : connections) {
                try {
                    if (connection != exclude) {
                        connection.connection.sendMessage(message);
                    }
                } catch (Exception e) {
                    pool.remove(connection);
                    logger.log(Level.SEVERE, "Sending message via " + connection + " failed -- connection removed", e);
                }
            }
        }
    }

    // *** Hook Implementations ***

    @Override
    public void init() {
        try {
            logger.info("##### Starting Jetty WebSocket server #####");
            server = new WebSocketsServer(WEBSOCKETS_PORT);
            server.start();
            // ### server.join();
            logger.info("### Jetty WebSocket server started successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Starting Jetty WebSocket server failed", e);
        }
    }

    @Override
    public void shutdown() {
        try {
            if (server != null) {
                logger.info("##### Stopping Jetty WebSocket server #####");
                server.stop();
            } else {
                logger.info("Stopping Jetty WebSocket server ABORTED -- not yet started");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Stopping Jetty WebSocket server failed", e);
        }
    }

    // ------------------------------------------------------------------------------------------------- Private Classes

    private class WebSocketsServer extends Server {

        private WebSocketsServer(int port) {
            // add connector
            Connector connector = new SelectChannelConnector();
            connector.setPort(port);
            addConnector(connector);
            //
            // set handler
            setHandler(new WebSocketHandler() {
                @Override
                public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
                    checkProtocol(protocol);
                    return new WebSocketConnection(protocol, pool, dm4);
                }
            });
        }

        private void checkProtocol(String pluginUri) {
            try {
                if (pluginUri == null) {
                    throw new RuntimeException("A plugin URI is missing in the WebSocket handshake -- Add your " +
                        "plugin's URI as the 2nd argument to the JavaScript WebSocket constructor");
                } else {
                    dm4.getPlugin(pluginUri);   // check plugin URI, throws if invalid
                }
            } catch (Exception e) {
                throw new RuntimeException("Opening a WebSocket connection " +
                    (pluginUri != null ? "for plugin \"" + pluginUri + "\" " : "") + "failed", e);
            }
        }
    }
}
