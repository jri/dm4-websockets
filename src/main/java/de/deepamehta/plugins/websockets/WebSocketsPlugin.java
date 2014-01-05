package de.deepamehta.plugins.websockets;

import de.deepamehta.core.Topic;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.Directives;
import de.deepamehta.core.service.event.PostUpdateTopicListener;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

import javax.servlet.http.HttpServletRequest;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;



public class WebSocketsPlugin extends PluginActivator implements PostUpdateTopicListener {

    // ------------------------------------------------------------------------------------------------------- Constants

    private static final int WEBSOCKETS_PORT = 8081;

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private TestServer server;

    private Logger logger = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods

    // *** Hook Implementations ***

    @Override
    public void init() {
        try {
            logger.info("##### Starting Jetty WebSockets #####");
            server = new TestServer(WEBSOCKETS_PORT);
            server.start();
            // ### server.join();
            logger.info("### Jetty WebSockets started successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Starting Jetty WebSockets failed");
        }
    }

    // *** Listener Implementations ***

    @Override
    public void postUpdateTopic(Topic topic, TopicModel newModel, TopicModel oldModel, ClientState clientState,
                                                                                       Directives directives) {
        server.broadcast(topic.toJSON().toString());
    }

    // ------------------------------------------------------------------------------------------------- Private Classes

    private class TestServer extends Server {

        private ConcurrentLinkedQueue<TestWebSocket> _broadcast = new ConcurrentLinkedQueue<TestWebSocket>();

        private TestServer(int port) {
            // add connector
            SelectChannelConnector connector = new SelectChannelConnector();
            connector.setPort(port);
            addConnector(connector);
            //
            // set WebSocket handler
            WebSocketHandler wsHandler = new WebSocketHandler() {
                @Override
                public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
                    return new TestWebSocket();
                }
            };
            setHandler(wsHandler);
        }

        private void broadcast(String message) {
            for (TestWebSocket ws : _broadcast) {
                try {
                    ws.getConnection().sendMessage(message);
                } catch (Exception e) {
                    _broadcast.remove(ws);
                    logger.log(Level.SEVERE, "Sending message to " + ws + " failed -- WebSocket removed", e);
                }
            }
        }

        private class TestWebSocket implements WebSocket, WebSocket.OnTextMessage, WebSocket.OnBinaryMessage,
                                                          WebSocket.OnFrame, WebSocket.OnControl {
            private FrameConnection _connection;

            private FrameConnection getConnection() {
                return _connection;
            }

            // *** WebSocket ***

            @Override
            public void onOpen(Connection connection) {
                System.err.printf("TestWebSocket#onOpen      %s\n", connection);
                _broadcast.add(this);
            }

            @Override
            public void onClose(int code, String message) {
                System.err.printf("TestWebSocket#onClose     %d %s\n", code, message);
                _broadcast.remove(this);
            }

            // *** WebSocket.OnTextMessage ***

            @Override
            public void onMessage(String data) {
                System.err.printf("TestWebSocket#onMessage   %s\n", data);
            }

            // *** WebSocket.OnBinaryMessage ***

            @Override
            public void onMessage(byte[] data, int offset, int length) {
                System.err.printf("TestWebSocket#onMessage   %s\n", TypeUtil.toHexString(data, offset, length));
            }

            // *** WebSocket.OnFrame ***

            @Override
            public boolean onFrame(byte flags, byte opcode, byte[] data, int offset, int length) {            
                System.err.printf("TestWebSocket#onFrame     %s|%s %s\n", TypeUtil.toHexString(flags),
                    TypeUtil.toHexString(opcode), TypeUtil.toHexString(data, offset, length));
                return false;
            }
        
            @Override
            public void onHandshake(FrameConnection connection) {
                System.err.printf("TestWebSocket#onHandshake %s\n", connection);
                _connection = connection;
            }

            // *** WebSocket.OnControl ***

            @Override
            public boolean onControl(byte controlCode, byte[] data, int offset, int length) {
                System.err.printf("TestWebSocket#onControl   %s %s\n", TypeUtil.toHexString(controlCode),
                    TypeUtil.toHexString(data, offset, length));            
                return false;
            }
        }
    }
}
