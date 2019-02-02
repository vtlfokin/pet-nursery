package com.example

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
import org.axonframework.eventhandling.EventHandler
import com.example.pet.RegisterNewPetCommand
import com.example.pet.VaccinatePetCommand
import pet.domain.Species
import pet.domain.PetRegistered
import pet.domain.PetVaccinated
import java.lang.IllegalArgumentException
import java.util.*

fun main(args: Array<String>) {

    val config = buildDefaultConfiguration()

    config.start()

    embeddedServer(Netty, 7000) {
        routing {
            get("/") {
                call.respondText("Hi", ContentType.Text.Html)
            }
            get("/pets") {
                call.respondText(PetQueryObjectRepository.all().toString(), ContentType.Text.Html)
            }
            post("/pet/register") {
                val params = call.receiveParameters()
                val name = params["name"]
                val typeKey = params["type"]

                if (null == name || null == typeKey) {
                    throw Exception("Fill name and type parameters")
                }

                val id = UUID.randomUUID().toString()
                val type = Species.valueOf(typeKey.toUpperCase())

                config.commandGateway().send<Unit>(RegisterNewPetCommand(id, name, type))
                call.respondText("{\"petId\":\"$id\"}", ContentType.Application.Json)
            }
            post("/pet/{petId}/vaccinate/{disease}") {
                val id = call.parameters["petId"]!!
                val diseaseKey = call.parameters["disease"]!!
                val disease = Disease.valueOf(diseaseKey.toUpperCase())

                try {
                    config.commandGateway().sendAndWait<Unit>(VaccinatePetCommand(id, disease))
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
            get("/pet/{petId}/vaccinations") {
                val id = call.parameters["petId"]!!
                val list = VaccinationQueryObjectRepository.findForPet(id)
                call.respondText(list.toString(), ContentType.Text.Html)
            }
            get("/vaccinations") {
                call.respondText(VaccinationQueryObjectRepository.all().toString(), ContentType.Text.Html)
            }
        }
    }.start(true)
}

//READ
data class PetQueryObject(val id: String, val type: Species, val name: String)
object PetQueryObjectRepository {
    private val list = arrayListOf<PetQueryObject>()

    fun add(pet: PetQueryObject) {
        list.add(pet)
    }

    fun all(): List<PetQueryObject> {
        return list.toList()
    }

    fun find(id: String): PetQueryObject? {
        return list.find { it.id == id }
    }
}

class PetQueryObjectUpdater {
    private val repository = PetQueryObjectRepository

    @EventHandler
    fun on(evt: PetRegistered) {
        repository.add(PetQueryObject(evt.petId, evt.type, evt.name))
    }
}


data class VaccinationQueryObject(val petId: String, val disease: Disease, val date: Date)
object VaccinationQueryObjectRepository {

    private val list = arrayListOf<VaccinationQueryObject>()

    fun add(vaccination: VaccinationQueryObject) {
        list.add(vaccination)
    }

    fun all(): List<VaccinationQueryObject> {
        return list.toList()
    }
    fun findForPet(petId: String): List<VaccinationQueryObject> {
        return list.filter { it.petId == petId }
    }
}

class VaccinationQueryObjectUpdater {
    private val repository = VaccinationQueryObjectRepository

    @EventHandler
    fun on(evt: PetVaccinated) {
        repository.add(VaccinationQueryObject(evt.petId, evt.disease, evt.date))
    }
}