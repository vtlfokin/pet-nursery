package pet.domain

import com.example.pet.PetReadyForTameCommand
import com.example.vaccination.Disease
import com.example.vaccination.Vaccination
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.AggregateRoot
import com.example.pet.RegisterNewPetCommand
import com.example.pet.VaccinatePetCommand
import java.text.SimpleDateFormat
import java.util.*

@AggregateRoot
class Pet() {
    @AggregateIdentifier
    private lateinit var petId: String
    private lateinit var name: String
    private lateinit var type: Species
    private lateinit var status: Status
    private val vaccinations = arrayListOf<Vaccination>()

    @CommandHandler
    constructor(cmd: RegisterNewPetCommand) : this() {
        AggregateLifecycle.apply(
            PetRegistered(
                cmd.id,
                cmd.name,
                cmd.type
            )
        )
    }

    @CommandHandler
    fun doVaccinate(cmd: VaccinatePetCommand) {
        val alreadyExistVaccinate = vaccinations.find { it.disease == cmd.disease }
        if (null == alreadyExistVaccinate) {
            AggregateLifecycle.apply(
                PetVaccinated(
                    cmd.petId,
                    cmd.disease,
                    Date()
                )
            )
        } else {
            val dateFormat = SimpleDateFormat("dd.mm.yyyy HH:mm:ss")
            throw IllegalStateException("Vaccination against ${cmd.disease.toString().toLowerCase()} already done at ${dateFormat.format(alreadyExistVaccinate.date)}")
        }

    }

    @CommandHandler
    fun readyForTame(cmd: PetReadyForTameCommand) {
        when (status) {
            Status.INCOME, Status.MEDICAL_EXAMINATION -> {
                AggregateLifecycle.apply(PetReadyForTame(cmd.petId))
            }
            else -> {}
        }
    }

    @EventSourcingHandler
    fun on(event: PetRegistered) {
        petId = event.petId
        name = event.name
        type = event.type

        status = Status.INCOME
    }

    @EventSourcingHandler
    fun on(event: PetVaccinated) {
        vaccinations.add(Vaccination(event.disease, event.date))
    }

    @EventSourcingHandler
    fun on(event: PetReadyForTame) {
        status = Status.ACCEPTED
    }

    enum class Status {
        INCOME, MEDICAL_EXAMINATION, ACCEPTED, TAMED, DEAD
    }
}

//EVENTS
data class PetRegistered(val petId: String, val name: String, val type: Species)

data class PetVaccinated(val petId: String, val disease: Disease, val date: Date)

data class PetReadyForTame(val petId: String)