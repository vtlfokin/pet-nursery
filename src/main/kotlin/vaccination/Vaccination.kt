package com.example.vaccination

import java.util.*

enum class Disease {
    DISTEMPER(),
    ENCEPHALITIS(),
    FRENZY()
}

data class Vaccination(val disease: Disease, val date: Date)