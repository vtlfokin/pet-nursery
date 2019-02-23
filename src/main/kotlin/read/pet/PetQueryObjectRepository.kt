package com.example.read.pet

object PetQueryObjectRepository {
    private val list = arrayListOf<PetQueryObject>()

    fun add(pet: PetQueryObject) {
        list.add(pet)
    }

    fun all(): List<PetQueryObject> {
        return list.toList()
    }

    fun find(id: String): PetQueryObject? {
        return list.find { it.id == id }
    }
}