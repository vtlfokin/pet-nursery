package com.example.write.tame

import write.customer.Customer
import com.example.write.pet.Pet
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.modelling.command.Repository
import javax.inject.Inject

class CustomerTamePetHandler {

    @Transient
    @Inject
    private val commandGateway: CommandGateway? = null

    @Transient
    @Inject
    private val petRepository: Repository<Pet>? = null

    @Transient
    @Inject
    private val customerRepository: Repository<Customer>? = null

    @CommandHandler
    fun handle(command: CustomerTamePetCommand) {
        val pet = petRepository!!.load(command.petId)
        val customer = customerRepository!!.load(command.customerId)

//        if (pet.rootType())
    }
}

data class CustomerTamePetCommand(val petId: String, val customerId: String)
