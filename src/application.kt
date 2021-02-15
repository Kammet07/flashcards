package com.kammet.flashcards.backend

import com.kammet.flashcards.backend.api.UserIdentity
import com.kammet.flashcards.backend.api.apiRouting
import com.kammet.flashcards.backend.api.identity
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.event.Level

//runs application
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

//application
@KtorExperimentalLocationsAPI
@Suppress("unused") //Referenced in application.conf
fun Application.module() {
    //connection to mariadb
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
    }


    //test
    /*
    transaction {
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
    }

    transaction {
        CollectionEntity.new {
            category = "test"
            public = false
            creatorId = UserEntity.findByUsername("vojtando")?.toUser()?.id ?: 1
        }
    }

    transaction {
        FlashcardEntity.new {
            term = "hello"
            definition = "привет"
            collectionId =
                CollectionEntity.findByCreatorId(UserEntity.findByUsername("vojtando")?.toUser()?.id ?: 1)?.toCollection()?.id ?: 1
        }

        FlashcardEntity.new {
            term = "welcome"
            definition = "vitám"
            collectionId =
                CollectionEntity.findByCreatorId(UserEntity.findByUsername("vojtando")?.toUser()?.id ?: 1)?.toCollection()?.id ?: 1
        }

                FlashcardEntity.new {
            term = "드림캐쳐"
            definition = "dreamcather"
            collectionId =
                CollectionEntity.findByCreatorId(UserEntity.findByUsername("vojtando")?.toUser()?.id ?: 1)?.toCollection()?.id ?: 1
        }

                FlashcardEntity.new {
            term = "قبض على"
            definition = "catch"
            collectionId =
                CollectionEntity.findByCreatorId(UserEntity.findByUsername("vojtando")?.toUser()?.id ?: 1)?.toCollection()?.id ?: 1
        }

    }
     */

    //sets content negotiator
    install(ContentNegotiation) {
        register(ContentType.Application.Json, GsonConverter(gson))
    }

    authentication {
        provider {
            pipeline.intercept(AuthenticationPipeline.CheckAuthentication) {
                val identity = call.identity
                val user = identity?.asEntity()
                if (user == null) {
                    if (identity != null) call.identity = null
                    call.response.status(HttpStatusCode.Unauthorized)
                    call.respondJson()
                }
            }
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

    install(Sessions) {
        cookie<UserIdentity>("LOGIN_SESSION") {
            val secretKey = hex("1fc2fd197f4e4c1eedc908cf4f54c070")
            transform(SessionTransportTransformerMessageAuthentication(secretKey))
        }
    }

}

fun connectToDb(dbName: String? = null): Database = Database.connect(
    "jdbc:mysql://127.0.0.1:3306/${dbName.orEmpty()}?characterEncoding=utf8&useUnicode=true",
    "org.mariadb.jdbc.Driver",
    "card",
    "flashcards135"
)
