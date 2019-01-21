package com.example

import com.example.vaccination.Disease
import com.example.vaccination.Vaccination
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receiveParameters
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.config.DefaultConfigurer
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.AggregateRoot
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

fun main(args: Array<String>) {
    val config = DefaultConfigurer.defaultConfiguration()
        .configureAggregate(Pet::class.java)
        .configureEmbeddedEventStore { InMemoryEventStorageEngine() }
        .eventProcessing {
            // Регистрация слушателей событий (для обновления read модели)
            it.registerEventHandler{ PetQueryObjectUpdater() }
            it.registerEventHandler{ VaccinationQueryObjectUpdater() }
        }
        .buildConfiguration()

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
                call.respondText("{\"id\":\"$id\"}", ContentType.Application.Json)
            }
            post("/pet/{id}/vaccinate/{disease}") {
                val id = call.parameters["id"]!!
                val diseaseKey = call.parameters["disease"]!!
                val disease = Disease.valueOf(diseaseKey.toUpperCase())

                config.commandGateway().send<Unit>(VaccinatePetCommand(id, disease))
                call.respondText("OK", ContentType.Text.Html)
            }
            get("/pet/{id}/vaccinations") {
                val id = call.parameters["id"]!!
                val list = VaccinationQueryObjectRepository.findForPet(id)
                call.respondText(list.toString(), ContentType.Text.Html)
            }
            get("/vaccinations") {
                call.respondText(VaccinationQueryObjectRepository.all().toString(), ContentType.Text.Html)
            }
        }
    }.start(true)
}

enum class Species {
    CAT(),
    DOG()
}

@AggregateRoot
class Pet() {
    @AggregateIdentifier
    private lateinit var petId: String
    private lateinit var name: String
    private lateinit var type: Species
    private val vaccinations = arrayListOf<Vaccination>()

    @CommandHandler
    constructor(cmd: RegisterNewPetCommand) : this() {
        AggregateLifecycle.apply(PetRegisteredEvent(cmd.id, cmd.name, cmd.type))
    }

    @CommandHandler
    fun doVaccinate(cmd: VaccinatePetCommand) {
        val date = Date()
        AggregateLifecycle.apply(PetVaccinatedEvent(cmd.petId, cmd.disease, date))
    }

    @EventSourcingHandler
    fun on(event: PetRegisteredEvent) {
        petId = event.id
        name = event.name
        type = event.type
    }

    @EventSourcingHandler
    fun on(event: PetVaccinatedEvent) {
        vaccinations.add(Vaccination(event.disease, event.date))
    }
}

//EVENTS
data class PetRegisteredEvent(val id: String, val name: String, val type: Species)

data class PetVaccinatedEvent(val petId: String, val disease: Disease, val date: Date)

//COMMANDS
data class RegisterNewPetCommand(val id: String, val name: String, val type: Species) {
    init {
        if (name.length < 2) {
            throw Exception("Name can be at least 2 symbols")
        }
    }
}

data class VaccinatePetCommand(@TargetAggregateIdentifier val petId: String, val disease: Disease)

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
    fun on(evt: PetRegisteredEvent) {
        repository.add(PetQueryObject(evt.id, evt.type, evt.name))
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
    fun on(evt: PetVaccinatedEvent) {
        repository.add(VaccinationQueryObject(evt.petId, evt.disease, evt.date))
    }
}