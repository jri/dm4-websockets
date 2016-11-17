
DeepaMehta 4 WebSockets
=======================

A DeepaMehta 4 plugin which provides the basis for handling WebSockets.

* Launches a WebSocket server on port 8081.
* Provides a service to let plugins broadcast messages to WebSocket clients (server push).
* Defines server-side events to let plugins react upon incoming WebSocket messages.

A simple WebSockets demo application:  
<https://github.com/jri/dm4-websockets-demo>

DeepaMehta 4 is a platform for collaboration and knowledge management.  
<https://github.com/jri/deepamehta>


Version History
---------------

**0.4** -- Nov 17, 2016

* 2 config options: `dm4.websockets.port` and `dm4.websockets.url`
* Client-side support for auto-reconnecting dropped connections
* Extended service API: send message over a particular client connection

**0.3** -- Apr 18, 2016

* New service call: broadcast to all but origin client.
* More informative error reporting when server-side message processing fails.
* Compatible with DeepaMehta 4.8

**0.2.3** -- Mar 22, 2016

* Compatible with DeepaMehta 4.7

**0.2.2** -- Dec 3, 2014

* Tiny bundle size (makes use of the Jetty 8 server included in DeepaMehta 4.4)
* Compatible with DeepaMehta 4.4

**0.2.1** -- Jun 8, 2014

* Compatible with DeepaMehta 4.3

**0.2** -- Feb 18, 2014

* Extensibility:
  * Broadcast service (server push)
  * Server-side events for reacting upon incoming messages
* Compatible with DeepaMehta 4.2

**0.1** -- Jan 8, 2014

* Pure tech demo, functional, performs no useful task, not extensible
* Includes a Jetty 7 server
* Compatible with DeepaMehta 4.2-SNAPSHOT


------------
Jörg Richter  
Nov 17, 2016
