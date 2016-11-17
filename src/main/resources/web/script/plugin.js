dm4c.add_plugin("de.deepamehta.websockets", function() {

    var url = dm4c.restc.request("GET", "/websockets")["dm4.websockets.url"]

    console.log("CONFIG: the WebSocket server is reachable at", url)

    /**
     * Creates a WebSocket connection.
     *
     * The URL to connect to is determined automatically, based on the server-side `dm4.websockets.url` config property.
     * The created WebSocket auto-reconnects once timed out by the browser (usually every 5 minutes).
     * WebSocket messages are expected to be JSON. Serialization/Deserialization performs automatically.
     *
     * @param   plugin_uri
     *              the URI of the calling plugin.
     * @param   message_processor
     *              the function that processes incoming messages.
     *              One argument is passed: the message pushed by the server (a deserialzed JSON object).
     *
     * @return  The created WebSocket object, wrapped as a DM4 proprietary object.
     *          This object provides a "send_message" function which takes 1 argument: the message to be
     *          sent to the server. The argument will be automatically serialized as a JSON object.
     */
    this.create_websocket = function(plugin_uri, message_processor) {

        return new function() {

            var ws
            setup_websocket()

            this.send_message = function(message) {
                ws.send(JSON.stringify(message))
            }

            function setup_websocket() {

                ws = new WebSocket(url, plugin_uri)

                ws.onopen = function(e) {
                    console.log("Opening WebSocket connection to", e.target.url)
                }

                ws.onmessage = function(e) {
                    var message = JSON.parse(e.data)
                    console.log("Message received", message)
                    message_processor(message)
                }

                ws.onclose = function(e) {
                    console.log("Closing WebSocket connection to", e.target.url, "reason:", e.reason)
                    console.log("Reopening ...")
                    setTimeout(setup_websocket, 1000)
                }
            }
        }
    }
})
