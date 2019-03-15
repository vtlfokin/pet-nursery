package com.example.read.vaccination

import org.axonframework.eventhandling.EventHandler
import com.example.write.pet.PetVaccinated
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.Timestamp
import java.time.Instant
import java.util.*

@ProcessingGroup("pet.main")
class VaccinationQueryObjectUpdater {
    private val repository = VaccinationQueryObjectRepository

    @EventHandler
    fun on(evt: PetVaccinated, @Timestamp time: Instant) {
        val date = Date.from(time)
        repository.add(
            VaccinationQueryObject(
                evt.petId,
                evt.disease,
                date
            )
        )
    }
}