package de.deepamehta.plugins.websockets;

import de.deepamehta.core.osgi.PluginActivator;

import java.util.logging.Level;
import java.util.logging.Logger;



public class WebSocketsPlugin extends PluginActivator implements WebSocketsService {

    // ------------------------------------------------------------------------------------------------------- Constants

    private static final int WEBSOCKETS_PORT = 8081;

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private WebSocketsServer server;

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
            server = new WebSocketsServer(WEBSOCKETS_PORT, dms);
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
            logger.info("##### Stopping Jetty WebSocket server #####");
            server.stop();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Stopping Jetty WebSocket server failed", e);
        }
    }
}
