package com.kammet.flashcards.backend

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.features.*
import org.slf4j.event.*
import io.ktor.jackson.*
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    var db = connectToDb()

    install(ContentNegotiation) {
        jackson {

        }
    }

    routing {
        get("/") {
            call.respond(User("Pavel", 19))
        }


    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}

fun connectToDb(dbName: String? = null): Database = Database.connect(
    url = "jdbc:mysql://127.0.0.1:3306/${dbName.orEmpty()}",
    user = "card",
    password = "flashcard135",
    driver = "org.mariadb.jdbc.Driver"
)

data class User(val name: String, val age: Int)