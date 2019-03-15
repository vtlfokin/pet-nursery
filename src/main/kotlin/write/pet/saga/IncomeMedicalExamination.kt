package com.example.write.pet.saga

import com.example.write.pet.PetReadyForTameCommand
import write.vaccination.Disease
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.SagaLifecycle
import org.axonframework.modelling.saga.StartSaga
import com.example.write.pet.PetRegistered
import com.example.write.pet.PetVaccinated
import javax.inject.Inject

class IncomeMedicalExamination {

    @Transient
    @Inject
    private var commandGateway: CommandGateway? = null
    private val vaccines: HashSet<Disease> = HashSet()

    @StartSaga
    @SagaEventHandler(associationProperty = "petId")
    fun handle(event: PetRegistered) {
        println("Start IncomeMedicalExamination for pet ${event.name}(${event.petId})")
    }

    @SagaEventHandler(associationProperty = "petId")
    fun handle(event: PetVaccinated) {
        vaccines.add(event.disease)

        if (vaccines.containsAll(Disease.values().asList())) {
            commandGateway!!.send<Unit>(PetReadyForTameCommand(event.petId))
            SagaLifecycle.end()
        }
    }
}