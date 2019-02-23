package com.example.server.routing

import com.example.vaccination.Disease
import com.example.write.pet.domain.Species
import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route

fun Routing.registerOptions(gson: Gson) {
    route("/options") {

        get("/species") {
            call.respondText(gson.toJson(Species.values()), ContentType.Application.Json)
        }
        get("/disease") {
            call.respondText(gson.toJson(Disease.values()), ContentType.Application.Json)
        }
    }
}