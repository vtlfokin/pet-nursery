package com.example.write.pet

import com.example.vaccination.Disease
import org.axonframework.modelling.command.TargetAggregateIdentifier
import com.example.write.pet.domain.Species

data class RegisterNewPetCommand(val id: String, val name: String, val type: Species) {
    init {
        if (name.length < 2) {
            throw Exception("Name can be at least 2 symbols")
        }
    }
}

data class VaccinatePetCommand(@TargetAggregateIdentifier val petId: String, val disease: Disease)

data class PetReadyForTameCommand(@TargetAggregateIdentifier val petId: String)