package de.deepamehta.plugins.websockets.event;

import de.deepamehta.core.service.EventListener;



public interface WebsocketTextMessageListener extends EventListener {

    void websocketTextMessage(String message);
}
