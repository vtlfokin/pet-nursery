package com.example.read.pet

import org.axonframework.eventhandling.EventHandler
import com.example.write.pet.domain.PetRegistered
import org.axonframework.eventhandling.ResetHandler
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class PetQueryObjectUpdater {
    private val repository = PetQueryObjectRepository

    init {
        transaction {
            SchemaUtils.create(PetsSimple)
        }
    }

    @EventHandler
    fun on(evt: PetRegistered) {
        PetQueryObjectRepository.add(PetQueryObject(evt.petId, evt.type, evt.name))
    }

    @ResetHandler
    fun onReset() { // will be called before replay starts
        transaction {
            PetsSimple.dropStatement().forEach {
                exec(it)
            }

        }
    }
}