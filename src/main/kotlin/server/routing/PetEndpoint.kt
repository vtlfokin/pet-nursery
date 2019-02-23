package com.example.server.routing

import com.example.read.pet.PetQueryObject
import com.example.read.pet.PetQueryObjectRepository
import com.example.read.vaccination.VaccinationQueryObjectRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.registerPetEndpoints(gson: Gson) {
    get("/pets") {
        val petsType = object : TypeToken<List<PetQueryObject>>() {}.type
        call.respondText(
            gson.toJson(PetQueryObjectRepository.all(), petsType),
            ContentType.Application.Json
        )
    }

    get("/pet/{petId}/vaccinations") {
        val id = call.parameters["petId"]!!
        val list = VaccinationQueryObjectRepository.findForPet(id)
        call.respondText(list.toString(), ContentType.Text.Html)
    }
}