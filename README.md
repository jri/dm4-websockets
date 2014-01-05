
DeepaMehta 4 WebSockets
=======================

A DeepaMehta 4 plugin for testing bidirectional WebSockets by embedding a Jetty server.

DeepaMehta 4 is a platform for collaboration and knowledge management.  
<https://github.com/jri/deepamehta>


Run the test
------------

1. Install the plugin and start DeepaMehta.

2. Open the WebSocket client:  
   <http://localhost:8080/de.deepamehta.websockets/>

   A blank page (with a title) appears.  
   This page opens a WebSocket connection to `ws://localhost:8081/` and sends a message through it.

   In the server log you'll see:

        TestWebSocket#onHandshake WSFrameConnection@3ad6a0e0 l(127.0.0.1:8081)<->r(127.0.0.1:54532)
        TestWebSocket#onOpen      WSFrameConnection@3ad6a0e0 l(127.0.0.1:8081)<->r(127.0.0.1:54532)
        TestWebSocket#onFrame     08|01 48656c6c6f20576562536f636b65742073657276657221
        TestWebSocket#onMessage   Hello WebSocket server!

   The WebSocket connection is established and the server received the message.

3. In the WebSocket client: open the browser's web console.

4. In another browser window (or in another browser): open the DeepaMehta Webclient and log in.
   <http://localhost:8080/de.deepamehta.webclient/>

5. In the DeepaMehta Webclient: create or edit any topics.
   All topic updates events are broadcasted to all open WebSocket clients.
   The events appear in their web consoles.


------------
Jörg Richter  
Jan 5, 2014
