package com.kammet.flashcards.backend.api

import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.routing.*

@KtorExperimentalLocationsAPI
fun Application.apiRouting() = routing {
    route("/api") {
        userRoutes()
        authenticationRoutes()
        collectionRoutes()
        flashcardRoutes()
    }
}
