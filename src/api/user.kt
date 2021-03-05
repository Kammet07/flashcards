package com.kammet.flashcards.backend.api

import com.kammet.flashcards.backend.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteIgnoreWhere
import org.jetbrains.exposed.sql.transactions.transaction
import javax.validation.constraints.Email
import javax.validation.constraints.Size

@Location("/user")
class UserLocation {
    @Location("/{userId}")
    data class Detail(val userId: Long)
}

data class UserLoginModel(val username: String, val password: String)

interface IUserModel {
    val username: String
    val password: String?
    val mail: String?

    data class Edit(
        @Size(min = 5, max = UserEntity.Table.MAX_USERNAME_LENGTH)
        override val username: String,
        @Size(min = 5, max = UserEntity.Table.MAX_PASSWORD_LENGTH)
        override val password: String?,
        @Size(min = 5, max = UserEntity.Table.MAX_MAIL_LENGTH)
        @Email
        override val mail: String?
    ) : IUserModel

    data class Create(
        @Size(min = 5, max = UserEntity.Table.MAX_USERNAME_LENGTH)
        override val username: String,
        @Size(min = 5, max = UserEntity.Table.MAX_PASSWORD_LENGTH)
        override val password: String,
        @Size(min = 5, max = UserEntity.Table.MAX_MAIL_LENGTH)
        override val mail: String
    ) : IUserModel
}

data class UserViewModel(val id: Long, val username: String, val mail: String)

fun UserEntity.valuesFrom(model: IUserModel) {
    username = model.username
    model.password?.let { password = BCryptPasswords.hash(it) }
    mail = model.mail!!
}

fun UserEntity.asViewModel() = UserViewModel(id.value, username, mail)

fun Route.userRoutes() {
    /**
     * returns all users?
     *
     * TODO: remove before "releasing"?
     */
    get<UserLocation> {
        call.respond(transaction { UserEntity.all().map { it.asViewModel() } })
    }

    /**
     * creates user
     *
     * receives call for creation ->
     * validates constants from call ->
     * if didn't pass validation -> throws exception
     * else checks for username and mail duplicates and correctness of mail ->
     * if there is already user with this mail or username or mail format is wrong -> responds with code 409 and message
     * else creates user and responds with code 201 and view model of created user
     */
    post<UserLocation> {
        val model = call.receive<IUserModel.Create>().also { it.validate() }
        val user = transaction {
            when {
                UserEntity.existsByUsername(model.username) || UserEntity.existsByMail(model.mail) -> null
                else -> UserEntity.new { valuesFrom(model) }
            }
        }
        when (user) {
            null -> {
                call.response.status(HttpStatusCode.Conflict)
                call.respond("username or mail not unique or bad mail format")
            }
            else -> {
                call.response.status(HttpStatusCode.Created)
                call.respond(user.asViewModel())
            }
        }
    }

    /**
     * finds user by id
     *
     * receives call for search ->
     * requests user by id from database ->
     * if user exist responds with view model of requested user
     * else responds with code 404
     */
    get<UserLocation.Detail> { location ->
        val user = transaction { UserEntity.findById(location.userId) }
        call.respondOr404(user?.asViewModel())
    }

    /**
     *
     */
    authenticate {
        delete<UserLocation.Detail> { location ->
            call.forbiddenIf { call.identity?.id != location.userId }

            CollectionEntity.findByCreatorId(location.userId).forEach {
                transaction {
                    FlashcardEntity.Table.deleteIgnoreWhere { FlashcardEntity.Table.collection eq it.id.value }
                }
                transaction {
                    CollectionEntity.Table.deleteIgnoreWhere { CollectionEntity.Table.id eq it.id }
                }
            }

            transaction {
                UserEntity.Table.deleteIgnoreWhere { UserEntity.Table.id eq location.userId }
                call.identity = null
            }
            call.respond("removed")
        }

        put<UserLocation.Detail> { location ->
            call.forbiddenIf { call.identity?.id != location.userId }
            val model = call.receive<IUserModel.Edit>().also { it.validate() }
            val user = transaction {
                when {
                    UserEntity.existsByUsername(model.username) || !isEmailValid(model.mail!!) || UserEntity.existsByMail(
                        model.mail
                    ) -> null
                    else -> UserEntity.findById(location.userId)?.also { it.valuesFrom(model) }
                }
            }
            when (user) {
                null -> {
                    call.response.status(HttpStatusCode.Conflict)
                    call.respond("username or mail not unique or bad mail format")
                }
                else -> call.respond(user.asViewModel())
            }
        }
    }
}
