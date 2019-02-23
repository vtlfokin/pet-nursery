package com.example.read.vaccination

object VaccinationQueryObjectRepository {

    private val list = arrayListOf<VaccinationQueryObject>()

    fun add(vaccination: VaccinationQueryObject) {
        list.add(vaccination)
    }

    fun all(): List<VaccinationQueryObject> {
        return list.toList()
    }
    fun findForPet(petId: String): List<VaccinationQueryObject> {
        return list.filter { it.petId == petId }
    }
}