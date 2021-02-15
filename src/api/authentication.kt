package com.kammet.flashcards.backend.api

import com.kammet.flashcards.backend.BCryptPasswords
import com.kammet.flashcards.backend.UserEntity
import com.kammet.flashcards.backend.respondJson
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import org.jetbrains.exposed.sql.transactions.transaction

var ApplicationCall.identity: UserIdentity?
    get() = sessions.get<UserIdentity>()
    set(value) = when (value) {
        null -> sessions.clear<UserIdentity>()
        else -> sessions.set(value)
    }

@KtorExperimentalLocationsAPI
@Location("/authentication")
object AuthenticationLocation

@KtorExperimentalLocationsAPI
fun Route.authenticationRoutes() {
    post<AuthenticationLocation> {
        val model = call.receive<UserLoginModel>()
        val user = transaction {
            UserEntity.findByUsername(model.username)?.takeIf { BCryptPasswords.verify(it.password, model.password) }
        }
        call.identity = user?.let { UserIdentity(it.id.value) }
        when (user) {
            null -> {
                call.response.status(HttpStatusCode.BadRequest)
                call.respond("bad username or password")
            }
            else -> call.respond(user.asViewModel())
        }
    }

    authenticate {
        get<AuthenticationLocation> {
            call.respond(call.identity!!.asEntity()!!.asViewModel())
        }
    }

    delete<AuthenticationLocation> {
        call.identity = null
        call.respondJson { "logged out" }
    }
}