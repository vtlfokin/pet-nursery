package com.example.read.vaccination

import com.example.vaccination.Disease
import java.util.*

data class VaccinationQueryObject(val petId: String, val disease: Disease, val date: Date)