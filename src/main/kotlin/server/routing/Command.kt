package com.example.server.routing

import write.vaccination.Disease
import com.example.write.pet.RegisterNewPetCommand
import com.example.write.pet.VaccinatePetCommand
import com.example.write.pet.PetId
import write.pet.Species
import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.route
import org.axonframework.config.Configuration
import java.lang.IllegalArgumentException
import java.util.*

fun Routing.registerCommands(axonConfig: Configuration, gson: Gson) {
    route("/command") {
        post("/pet/register") {
            val request = gson.fromJson<PetRegisterRequest>(call.receiveText(), PetRegisterRequest::class.java)

            val id = UUID.randomUUID().toString()
            val type = Species.valueOf(request.type.toUpperCase())

            axonConfig.commandGateway().send<Unit>(RegisterNewPetCommand(id, request.name, type))

            call.respondText(gson.toJson(PetId(id)), ContentType.Application.Json)
        }
        post("/pet/{petId}/vaccinate/{disease}") {
            val id = call.parameters["petId"]!!
            val diseaseKey = call.parameters["disease"]!!
            val disease = Disease.valueOf(diseaseKey.toUpperCase())

            try {
                axonConfig.commandGateway().sendAndWait<Unit>(VaccinatePetCommand(id, disease))
                call.respondText("OK", ContentType.Text.Html)
            } catch (exception: Exception) {
                when (exception) {
                    is IllegalStateException, is IllegalArgumentException -> {
                        call.respondText(exception.message.toString(), ContentType.Text.Html)
                    }
                    else -> throw exception
                }
            }
        }
    }
}

data class PetRegisterRequest(val name: String, val type: String)