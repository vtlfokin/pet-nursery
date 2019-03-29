package com.example.read.vaccination

import write.vaccination.Disease
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import com.example.write.pet.PetRegistered
import com.example.write.pet.PetVaccinated
import write.pet.Species
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@ProcessingGroup("pet.main")
class VaccinationQueueProjector {

    private val repository = PetInVaccinationQueueRepository

    init {
        transaction {
            SchemaUtils.create(PetsInMedicalQueue)
        }
    }

    @EventHandler
    fun on(evt: PetRegistered) {
        println("QueueProjector: register ${evt.name}")

        repository.add(PetInVaccinationQueue(evt.petId, evt.name, evt.type))
    }

    @EventHandler
    fun on(evt: PetVaccinated) {
        repository.findByPetId(evt.petId)?.apply {
            registerVaccination(evt.disease)

            if (isNoMoreNeedVaccination()) {
                repository.remove(this)
            } else {
                val data = this
                transaction {
                    PetsInMedicalQueue.update({PetsInMedicalQueue.petId eq evt.petId}) { statement ->
                        statement[PetsInMedicalQueue.neededVaccines] = data.neededVaccines()
                            .joinToString(",") { it.toString() }
                    }
                }
            }
        }
    }

    @ResetHandler
    fun onReset() {
        println("QueueProjector: reset projector")

        repository.clear()
    }
}

// Забиваем известными на данный момент болезнями
data class PetInVaccinationQueue(val id: String, val name: String, val species: Species,
                                 val neededVaccines: HashSet<Disease> = Disease.values().toHashSet()) {

    fun registerVaccination(disease: Disease) {
        neededVaccines.remove(disease)
    }

    fun isNoMoreNeedVaccination(): Boolean
    {
        return neededVaccines.isEmpty()
    }

    fun neededVaccines() = neededVaccines.toTypedArray()
}

object PetsInMedicalQueue : Table("read_pets_medical_queue") {
    val petId = varchar("pet_id", 44).primaryKey()
    val species = enumerationByName("species", 63, Species::class)
    val petName = varchar("pet_name", 255)
    val neededVaccines = varchar("needed_vaccines", 255)
}

object PetInVaccinationQueueRepository {
    init {
        Database.connect("jdbc:postgresql://localhost:15432/nursery", "org.postgresql.Driver", "root", "root")
    }
    fun add(pet: PetInVaccinationQueue) {
        transaction {
            PetsInMedicalQueue.insert { dao ->
                dao[petId] = pet.id
                dao[species] = pet.species
                dao[petName] = pet.name
                dao[neededVaccines] = pet.neededVaccines().joinToString(",") { it.toString() }
            }
        }
    }

    fun remove(pet: PetInVaccinationQueue) {
        transaction {
            PetsInMedicalQueue.deleteWhere {
                PetsInMedicalQueue.petId eq pet.id
            }
        }
    }

    fun all(): List<PetInVaccinationQueue> {
        return transaction {
            PetsInMedicalQueue.selectAll().map {
                daoToObject(it)
            }
        }
    }

    fun findByPetId(id: String): PetInVaccinationQueue? {
        return transaction {
            PetsInMedicalQueue.select { PetsInMedicalQueue.petId eq id }.limit(1).firstOrNull()?.let {
                daoToObject(it)
            }
        }
    }

    fun clear() {
        transaction {
            PetsInMedicalQueue.dropStatement().forEach {
                exec(it)
            }
        }
    }

    private fun daoToObject(row: ResultRow) = PetInVaccinationQueue(
            row[PetsInMedicalQueue.petId],
            row[PetsInMedicalQueue.petName],
            row[PetsInMedicalQueue.species],
            row[PetsInMedicalQueue.neededVaccines].split(",").map { vaccine ->
                Disease.valueOf(vaccine)
            }.toHashSet()
        )
}