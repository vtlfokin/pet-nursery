package domain.pet

import write.vaccination.Disease
import com.example.write.pet.PetReadyForTameCommand
import com.example.write.pet.PetRegistered
import com.example.write.pet.PetVaccinated
import write.pet.Species
import com.example.write.pet.saga.IncomeMedicalExamination
import org.axonframework.test.saga.SagaTestFixture
import org.junit.Before
import kotlin.test.Test

class IncomeMedicalExaminationTest {
    private lateinit var fixture: SagaTestFixture<IncomeMedicalExamination>
    private val sagaId = "test1"
    private val petId = "pet1"

    @Before
    fun setUp() {
        fixture = SagaTestFixture<IncomeMedicalExamination>(IncomeMedicalExamination::class.java)
    }

    @Test
    fun whileNotAllVaccinationsDoNothing() {
        fixture.givenAggregate(sagaId).published(
            PetRegistered(petId, "Fluffy", Species.CAT),
            PetVaccinated(petId, Disease.FRENZY)
        ).whenPublishingA(PetVaccinated(petId, Disease.ENCEPHALITIS))
            .expectNoDispatchedCommands()
    }

    @Test
    fun examinationNeedAllVaccinations() {
        fixture.givenAggregate(sagaId)
            .published(
                PetRegistered(petId, "Fluffy", Species.CAT),
                PetVaccinated(petId, Disease.FRENZY),
                PetVaccinated(petId, Disease.DISTEMPER)
            )
            .whenPublishingA( PetVaccinated(petId, Disease.ENCEPHALITIS) )
            .expectDispatchedCommands( PetReadyForTameCommand(petId) )
    }
}