package com.kammet.flashcards.backend.api

import com.kammet.flashcards.backend.UserEntity
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import javax.validation.constraints.Size

@KtorExperimentalLocationsAPI
@Location("/user")
class UserLocation {
    @Location("/{id}")
    data class Detail(val id: Long)
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

data class UserViewModel(val id: Long, val username: String)

fun UserEntity.valuesFrom(model: IUserModel) {
    username = model.username
    model.password
}

@KtorExperimentalLocationsAPI
fun Route.userRoutes() {
    get<UserLocation.Detail> {

    }
}

