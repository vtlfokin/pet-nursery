package com.example.read.pet

import com.example.write.pet.domain.Species

//READ
data class PetQueryObject(val id: String, val species: Species, val name: String)