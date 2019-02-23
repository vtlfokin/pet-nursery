package com.example.server.routing

import com.example.read.vaccination.PetInVaccinationQueue
import com.example.read.vaccination.PetInVaccinationQueueRepository
import com.example.read.vaccination.VaccinationQueryObjectRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.registerVaccineEndpoint(gson: Gson) {
    get("/vaccinate/queue") {
        val petsType = object : TypeToken<List<PetInVaccinationQueue>>() {}.type

        call.respondText(
            gson.toJson(PetInVaccinationQueueRepository.all(), petsType),
            ContentType.Application.Json
        )
    }

    get("/vaccinations") {
        call.respondText(VaccinationQueryObjectRepository.all().toString(), ContentType.Text.Html)
    }
}