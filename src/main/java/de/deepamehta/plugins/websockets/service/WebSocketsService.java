package de.deepamehta.plugins.websockets.service;

import de.deepamehta.core.service.PluginService;



public interface WebSocketsService extends PluginService {

    void broadcast(String pluginUri, String message);
}
