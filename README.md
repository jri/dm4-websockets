
DeepaMehta 4 WebSockets
=======================

A DeepaMehta 4 plugin for testing WebSockets via embedding a Jetty server.

DeepaMehta 4 is a platform for collaboration and knowledge management.  
<https://github.com/jri/deepamehta>


Run the test
------------

1. Open this page:  
   <http://localhost:8080/de.deepamehta.websockets>

   A blank page (with a title) appears.  
   This page opens a WebSocket connection (to port 8081) and sends a message through it.

2. In the server log you'll see:

        TestWebSocket#onHandshake WSFrameConnection@3ad6a0e0 l(127.0.0.1:8081)<->r(127.0.0.1:54532)
        TestWebSocket#onOpen      WSFrameConnection@3ad6a0e0 l(127.0.0.1:8081)<->r(127.0.0.1:54532)
        TestWebSocket#onFrame     08|01 48656c6c6f20576562536f636b657421
        TestWebSocket#onMessage   Hello WebSocket!

   The WebSocket received the message.


------------
Jörg Richter  
Jan 5, 2014
