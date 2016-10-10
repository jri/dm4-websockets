dm4c.add_plugin("de.deepamehta.websockets", function() {

    /**
     * Creates a WebSocket connection.
     * The created WebSocket auto-reconnects once timed out by the browser (usually every 5 minutes).
     * WebSocket messages are expected to be JSON. Serialization/Deserialization performs automatically.
     *
     * @param   url
     *              the URL to connect to.
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
    this.create_websocket = function(url, plugin_uri, message_processor) {

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
