package com.kammet.flashcards.backend

import com.google.gson.GsonBuilder
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import org.mindrot.jbcrypt.BCrypt
import java.text.DateFormat
import java.util.regex.Pattern
import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import kotlin.collections.Collection

object BCryptPasswords {
    fun verify(hash: String, password: String): Boolean = try {
        BCrypt.checkpw(password, hash)
    } catch (e: Throwable) {
        println(e)
        false
    }

    fun hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())
}

suspend fun ApplicationCall.respondOr404(message: Any?) {
    if (message == null) {
        response.status(HttpStatusCode.NotFound)
        respond("entity not found")
    } else {
        respond(message)
    }
}

val validator: Validator = Validation.buildDefaultValidatorFactory().validator

class ValidationException(val collection: Collection<ConstraintViolation<*>>) : Exception()

@Throws(ValidationException::class)
fun Any.validate() {
    val errors = validator.validate(this)
    if (errors.isNotEmpty()) {
        throw ValidationException(errors)
    }
}

suspend inline fun ApplicationCall.forbiddenIf(block: () -> Boolean) {
    if (block()) {
        response.status(HttpStatusCode.Forbidden)
        respond("forbidden")
    }
}

suspend fun ApplicationCall.respondJson(block: () -> Any? = { "" }) = respondText(ContentType.Application.Json) {
    gson.toJson(block())
}

val gson = GsonBuilder().apply {
    setDateFormat(DateFormat.LONG)
    setPrettyPrinting()
}.create()


fun isEmailValid(email: String): Boolean {
    return Pattern.compile(
        "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
    ).matcher(email).matches()
}