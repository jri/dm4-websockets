package de.deepamehta.plugins.websockets.event;

import de.deepamehta.core.service.Listener;



public interface WebsocketTextMessageListener extends Listener {

    void websocketTextMessage(String message);
}
