package com.example.write.customer.domain

import com.example.write.customer.ApproveCustomer
import com.example.write.customer.ChangeCustomerRevenue
import com.example.write.customer.FillCustomerProfile
import com.example.write.customer.RegisterCustomer
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.AggregateMember
import org.axonframework.modelling.command.AggregateRoot
import org.axonframework.modelling.command.EntityId
import java.util.*

@AggregateRoot
class Customer() {

    private lateinit var id: String
    private lateinit var name: String
    private var approved = false

    @AggregateMember
    private var profile: CustomerProfile? = null

    @CommandHandler
    constructor(cmd: RegisterCustomer): this() {
        AggregateLifecycle.apply(CustomerRegistered(cmd.id, cmd.name))
    }

    @CommandHandler
    fun approve(cmd: ApproveCustomer) {
        if (null == profile) {
            throw IllegalStateException("Cant approve customer without profile")
        }
        if (!approved) {
            AggregateLifecycle.apply(CustomerApproved(id))
        }
    }

    @CommandHandler
    fun fillProfile(cmd: FillCustomerProfile) {
        if (null != profile) {
            throw IllegalStateException("Profile Already filled")
        }

        AggregateLifecycle.apply(CustomerProfileFilled(id, cmd.birthDate, cmd.revenue))
    }

    @EventSourcingHandler
    fun on(event: CustomerRegistered) {
        id = event.id
        name = event.name
        approved = false
    }

    @EventSourcingHandler
    fun on(event: CustomerApproved) {
        approved = true
    }

    @EventSourcingHandler
    fun on(event: CustomerProfileFilled) {
        profile = CustomerProfile(id, event.birthDate, event.revenue)
    }
}

class CustomerProfile(@EntityId val customerId: String, private var birthDate: Date, private var revenue: Int) {
    @CommandHandler
    fun changeRevenue(cmd: ChangeCustomerRevenue) {
        AggregateLifecycle.apply(CustomerRevenueChanged(customerId, cmd.revenue))
    }

    @EventSourcingHandler
    fun on(event: CustomerRevenueChanged) {
        revenue = event.revenue
    }
}

// Events
data class CustomerRegistered(val id: String, val name: String)

data class CustomerApproved(val customerId: String)

data class CustomerProfileFilled(val customerId: String, val birthDate: Date, val revenue: Int)

data class CustomerRevenueChanged(val customerId: String, val revenue: Int)
