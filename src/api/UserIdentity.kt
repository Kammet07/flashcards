package com.kammet.flashcards.backend.api

import com.kammet.flashcards.backend.UserEntity
import org.jetbrains.exposed.sql.transactions.transaction

data class UserIdentity(val id: Long) {
    fun asEntity(): UserEntity? = transaction {
        UserEntity.findById(this@UserIdentity.id)
    }
}
