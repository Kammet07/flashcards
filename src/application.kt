package com.kammet.flashcards.backend

import com.kammet.flashcards.backend.api.apiRouting
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.features.*
import org.slf4j.event.*
import io.ktor.jackson.*
import io.ktor.locations.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

//runs application
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

//application
@Suppress("unused") //Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    //connection to mariadb >  >
    connectToDb()
    //if doesn't exist, creates schema
    transaction {
        SchemaUtils.createDatabase("test")
    }
    //connects to created/existing db
    connectToDb("test")
    //transactions in created db
    transaction {
        //creates missing tables
        SchemaUtils.createMissingTablesAndColumns(*entities)

        //test
        /*
        UserEntity.new {
            username = "vojtando"
            password = "rshnuydeirtdhuyioftpgdhnlqfpuyne"
            mail = "vojtando@mail.afrika"
        }

        UserEntity.new {
            username = "maxiiiik"
            password = "nseioradfglj8493q"
            mail = "maksimilian@jando.fun"
        }

        CollectionEntity.new {
            category = "test"
            public = false
            creatorId = 7
        }

        FlashcardEntity.new {
            term = "hello"
            definition = "привет"
            collectionId = 7
        }

        FlashcardEntity.new {
            term = "welcome"
            definition = "vitám"
            collectionId = 7
        }

        */
    }

    //basic routing - test
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

        route("/collection") {

        }
    }

    //sets jackson as content negotiator
    install(ContentNegotiation) {
        jackson {

        }
    }

    //logging while running
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(StatusPages) {

    }

    install(Locations)
    apiRouting()
}

fun connectToDb(dbName: String? = null): Database = Database.connect(
    "jdbc:mysql://127.0.0.1:3306/${dbName.orEmpty()}",
    "org.mariadb.jdbc.Driver",
    "card",
    "flashcards135"
)

data class User(val id: Long, val username: String, val password: String, val mail: String)

data class Collection(val id: Long, val category: String, val public: Boolean, val creatorId: Long)

data class Flashcard(val id: Long, val term: String, val definition: String, val collectionId: Long)

fun UserEntity.toUser(): User =
    User(
        id = id.value,
        username = username,
        password = password,
        mail = mail
    )

fun CollectionEntity.toCollection(): Collection =
    Collection(
        id = id.value,
        category = category,
        public = public,
        creatorId = creatorId
    )

fun FlashcardEntity.toFlashcard(): Flashcard =
    Flashcard(
        id = id.value,
        term = term,
        definition = definition,
        collectionId = collectionId
    )