package com.example

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

fun main(args: Array<String>) {

    val axonConfig = buildDefaultConfiguration()

    axonConfig.start()

    val gson = Gson()

    embeddedServer(Netty, 7000) {
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
        }
    }.start(true)
}