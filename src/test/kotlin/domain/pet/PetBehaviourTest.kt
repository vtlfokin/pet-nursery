package domain.pet

import com.example.vaccination.Disease
import com.example.write.pet.VaccinatePetCommand
import org.axonframework.test.aggregate.FixtureConfiguration
import kotlin.test.Test
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.Before
import com.example.write.pet.domain.Pet
import com.example.write.pet.domain.PetRegistered
import com.example.write.pet.domain.PetVaccinated
import com.example.write.pet.domain.Species
import java.time.Instant

class PetBehaviourTest {
    private lateinit var fixture: FixtureConfiguration<Pet>
    private val petId = "test1"

    @Before
    fun setUp() {
        fixture = AggregateTestFixture<Pet>(Pet::class.java)
    }

    @Test
    fun petCanBeVaccinated() {
        fixture.given(PetRegistered(petId, "Fluffy", Species.CAT))
            .`when`(VaccinatePetCommand(petId, Disease.FRENZY))
            .expectSuccessfulHandlerExecution()
            .expectEvents(PetVaccinated(petId, Disease.FRENZY))
    }

    @Test
    fun petCantBeVaccinatedTwiceFromOneDisease() {
        fixture.given(
            PetRegistered(petId, "Fluffy", Species.CAT),
            PetVaccinated(petId, Disease.FRENZY)
        )
            .`when`(VaccinatePetCommand(petId, Disease.FRENZY))
            .expectNoEvents()
            .expectException(IllegalStateException::class.java)
    }
}