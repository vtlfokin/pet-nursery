package com.example.read.vaccination

import com.example.vaccination.Disease
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import com.example.write.pet.domain.PetRegistered
import com.example.write.pet.domain.PetVaccinated

@ProcessingGroup("pet.main")
class VaccinationQueueProjector {

    private val repository = PetInVaccinationQueueRepository

    @EventHandler
    fun on(evt: PetRegistered) {
        println("QueueProjector: register ${evt.name}")

        repository.add(PetInVaccinationQueue(evt.petId, evt.name))
    }

    @EventHandler
    fun on(evt: PetVaccinated) {
        repository.findByPetId(evt.petId)?.apply {
            registerVaccination(evt.disease)

            if (isNoMoreNeedVaccination()) {
                repository.remove(this)
            }
        }
    }

    @ResetHandler
    fun onReset() {
        println("QueueProjector: reset projector")

        repository.clear()
    }
}

data class PetInVaccinationQueue(val petId: String, val petName: String) {
    // Забиваем известными на данный момент болезнями
    private val neededVaccines: HashSet<Disease> = Disease.values().toHashSet()

    fun registerVaccination(disease: Disease) {
        neededVaccines.remove(disease)
    }

    fun isNoMoreNeedVaccination(): Boolean
    {
        return neededVaccines.isEmpty()
    }

    fun neededVaccines() = neededVaccines.toTypedArray()
}

object PetInVaccinationQueueRepository {

    private val list = arrayListOf<PetInVaccinationQueue>()

    fun add(pet: PetInVaccinationQueue) {
        list.add(pet)
    }

    fun remove(pet: PetInVaccinationQueue) {
        list.remove(pet)
    }

    fun all(): List<PetInVaccinationQueue> {
        return list.toList()
    }

    fun findByPetId(id: String): PetInVaccinationQueue? {
        return list.firstOrNull { it.petId == id }
    }

    fun clear() {
        list.clear()
    }
}