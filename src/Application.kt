package com.kammet.flashcards.backend

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.features.*
import org.slf4j.event.*
import io.ktor.jackson.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    connectToDb()

    transaction {
        SchemaUtils.createDatabase("test")
    }

    connectToDb("test")

    transaction {
        addLogger(StdOutSqlLogger)

        SchemaUtils.createMissingTablesAndColumns(*entities)

//        UserEntity.new {
//            username = "vojtando"
//            password = "rshnuydeirtdhuyioftpgdhnlqfpuyne"
//            mail = "vojtando@mail.afrika"
//        }
//
//        UserEntity.new {
//            username = "maxiiiik"
//            password = "nseioradfglj8493q"
//            mail = "maksimilian@jando.fun"
//        }
    }

    install(Routing) {
        route("/users") {
            get("/") {
                val users = transaction {
                    UserEntity.all().map {
                        it.toUser()
                    }
                }
                call.respond(users)
            }
        }

    }

    install(ContentNegotiation) {
        jackson {

        }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(StatusPages) {

    }
}

fun connectToDb(dbName: String? = null): Database = Database.connect(
    "jdbc:mysql://127.0.0.1:3306/${dbName.orEmpty()}",
    "org.mariadb.jdbc.Driver",
    "card",
    "flashcards135"
)

data class User(val id: Long, val username: String, val password: String, val mail: String)

fun UserEntity.toUser(): User =
    User(
        id = id.value,
        username = username,
        password = password,
        mail = mail
    )