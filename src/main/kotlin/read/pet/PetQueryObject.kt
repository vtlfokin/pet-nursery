package com.example.read.pet

import write.pet.Species

//READ
data class PetQueryObject(val id: String, val species: Species, val name: String)