package com.example.read.pet

import org.axonframework.eventhandling.EventHandler
import com.example.write.pet.domain.PetRegistered

class PetQueryObjectUpdater {
    private val repository = PetQueryObjectRepository

    @EventHandler
    fun on(evt: PetRegistered) {
        PetQueryObjectRepository.add(PetQueryObject(evt.petId, evt.type, evt.name))
    }
}