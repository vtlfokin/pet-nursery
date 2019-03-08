package com.example

import com.example.server.SubscribeEvent
import com.example.server.WsSubscribeRegistry
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.axonframework.eventhandling.TrackingEventProcessor
import com.example.server.routing.registerCommands
import com.example.server.routing.registerOptions
import com.example.server.routing.registerPetEndpoints
import com.example.server.routing.registerVaccineEndpoint
import com.google.gson.Gson
import io.ktor.application.install
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach

val subscribeRegistry = WsSubscribeRegistry()
val gson = Gson()

@ObsoleteCoroutinesApi
fun main(args: Array<String>) {

    val axonConfig = buildDefaultConfiguration()

    axonConfig.start()

    embeddedServer(Netty, 7000) {
        install(WebSockets)

        routing {
            get("/") {
                call.respondText("Hi", ContentType.Text.Html)
            }
            registerOptions(gson)

            registerCommands(axonConfig, gson)

            registerPetEndpoints(gson)

            registerVaccineEndpoint(gson)

            get("/vaccinate/reset") {
                axonConfig.eventProcessingConfiguration()
                    .eventProcessorByProcessingGroup("vaccination", TrackingEventProcessor::class.java)
                    .ifPresent { processor ->
                        processor.shutDown()
                        processor.resetTokens()
                        processor.start()
                    }
                call.respondText("Reset done", ContentType.Text.Html)
            }

            webSocket("/subscribe") {
                try {
                    // We starts receiving messages (frames).
                    // Since this is a coroutine. This coroutine is suspended until receiving frames.
                    // Once the connection is closed, this consumeEach will finish and the code will continue.
                    incoming.consumeEach { frame ->
                        // Frames can be [Text], [Binary], [Ping], [Pong], [Close].
                        // We are only interested in textual messages, so we filter it.
                        if (frame is Frame.Text) {
                            // Now it is time to process the text sent from the user.
                            // At this point we have context about this connection, the session, the text and the server.
                            // So we have everything we need.
                            val incomeText = frame.readText()

                            if (incomeText.startsWith("/subscribe ")) {
                                val eventKey = incomeText.removePrefix("/subscribe ").trim().toLowerCase()

                                val event = SubscribeEvent.values().firstOrNull {
                                    it.key == eventKey
                                }

                                if (null != event) {
                                    subscribeRegistry.subscribe(event, this)
                                }
                            } else if (incomeText.startsWith("/unsubscribe ")) {
                                val eventKey = incomeText.removePrefix("/unsubscribe ").trim().toLowerCase()

                                val event = SubscribeEvent.values().firstOrNull {
                                    it.key == eventKey
                                }

                                if (null != event) {
                                    subscribeRegistry.unsubscribe(event, this)
                                }
                            }
                        }
                    }
                } finally {
                    // Either if there was an error, of it the connection was closed gracefully.
                    // We notify the server that the member left.
                    subscribeRegistry.unsubscribeSession(this)
                }
            }
        }
    }.start(true)
}