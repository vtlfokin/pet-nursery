package com.example.write.customer

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class RegisterCustomer(val id: String, val name: String)

data class ApproveCustomer(@TargetAggregateIdentifier val customerId: String)

data class FillCustomerProfile(@TargetAggregateIdentifier val customerId: String,
                               val birthDate: Date, val revenue: Int)

data class ChangeCustomerRevenue(@TargetAggregateIdentifier val customerId: String, val revenue: Int)