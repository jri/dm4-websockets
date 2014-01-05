package de.deepamehta.plugins.websockets;

import de.deepamehta.core.osgi.PluginActivator;

import org.eclipse.jetty.server.Server;

import java.util.logging.Logger;



public class WebSocketsPlugin extends PluginActivator {

    // ------------------------------------------------------------------------------------------------------- Constants

    private static final int WEBSOCKETS_PORT = 8081;

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private Logger logger = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods

    // *** Hook Implementations ***

    @Override
    public void init() {
        logger.info("##### Starting Jetty WebSockets #####");
        new TestServer(WEBSOCKETS_PORT);
    }

    // ------------------------------------------------------------------------------------------------- Private Classes

    public class TestServer extends Server {

        public TestServer(int port) {
        }
    }
}
