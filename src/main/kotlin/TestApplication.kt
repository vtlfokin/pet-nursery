package com.example

import com.example.read.pet.PetQueryObject
import com.example.vaccination.Disease
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receiveParameters
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import com.example.write.pet.RegisterNewPetCommand
import com.example.write.pet.VaccinatePetCommand
import com.example.read.vaccination.PetInVaccinationQueueRepository
import org.axonframework.eventhandling.TrackingEventProcessor
import com.example.write.pet.domain.Species
import com.example.read.pet.PetQueryObjectRepository
import com.example.read.vaccination.PetInVaccinationQueue
import com.example.read.vaccination.VaccinationQueryObjectRepository
import com.example.server.routing.registerCommands
import com.example.server.routing.registerOptions
import com.example.write.pet.domain.PetId
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.axonframework.serialization.json.JacksonSerializer
import java.lang.IllegalArgumentException
import java.util.*

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

            get("/pets") {
                val petsType = object : TypeToken<List<PetQueryObject>>() {}.type
                call.respondText(
                    gson.toJson(PetQueryObjectRepository.all(), petsType),
                    ContentType.Application.Json
                )
            }
            get("/vaccinate/queue") {
                val petsType = object : TypeToken<List<PetInVaccinationQueue>>() {}.type

                call.respondText(
                    gson.toJson(PetInVaccinationQueueRepository.all(), petsType),
                    ContentType.Application.Json
                )
            }
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
            get("/vaccinations") {
                call.respondText(VaccinationQueryObjectRepository.all().toString(), ContentType.Text.Html)
            }
            get("/pet/{petId}/vaccinations") {
                val id = call.parameters["petId"]!!
                val list = VaccinationQueryObjectRepository.findForPet(id)
                call.respondText(list.toString(), ContentType.Text.Html)
            }
        }
    }.start(true)
}