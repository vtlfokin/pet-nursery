package com.example.vaccination

import java.time.Instant

enum class Disease {
    DISTEMPER(),
    ENCEPHALITIS(),
    FRENZY()
}

data class Vaccination(val disease: Disease, val time: Instant)