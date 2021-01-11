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
//        SchemaUtils.create(Users)
//
//        Users.insert {
//            it[Users.username] = "Kammet"
//            it[Users.mail] = "somedude@mail.com"
//            it[Users.pass] = "test123"
//            it[Users.role] = false
//        }
//
//        Users.insert {
//            it[Users.username] = "admin"
//            it[Users.mail] = "someguy@mail.com"
//            it[Users.pass] = "test123"
//            it[Users.role] = true
//        }
    }

    install(Routing) {
        route("/users") {
            get("/") {
                val users = transaction {
                    Users.selectAll().map { Users.toUser(it) }
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
}

fun connectToDb(dbName: String? = null): Database = Database.connect(
    "jdbc:mysql://127.0.0.1:3306/${dbName.orEmpty()}",
    "org.mariadb.jdbc.Driver",
    "card",
    "flashcards135"
)

data class User(val id: Int? = null, val username: String, val pass: String, val mail: String, val role: Boolean)

object Users : Table() {
    val id: Column<Int> = integer("id").autoIncrement()
    val username: Column<String> = varchar("username", 20).uniqueIndex()
    val pass: Column<String> = varchar("pass", 255)
    val mail: Column<String> = varchar("mail", 320).uniqueIndex()
    val role: Column<Boolean> = bool("role")

    override val primaryKey = PrimaryKey(id, name = "PK_User_ID")

    fun toUser(row: ResultRow): User =
        User(
            id = row[Users.id],
            username = row[Users.username],
            pass = row[Users.pass],
            mail = row[Users.mail],
            role = row[Users.role]

        )
}