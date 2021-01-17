package com.kammet.flashcards.backend

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

val entities = arrayOf(UserEntity.Table, CollectionEntity.Table, FlashcardEntity.Table)

class UserEntity(id: EntityID<Long>) : Entity<Long>(id) {
    var username by Table.username
    var password by Table.password
    var mail by Table.mail

    object Table : LongIdTable("users") {
        val username = varchar("username", 20).uniqueIndex()
        val password = varchar("password", 255)
        val mail = varchar("mail", 320).uniqueIndex()

        override val primaryKey: PrimaryKey = PrimaryKey(id)
    }

    companion object : EntityClass<Long, UserEntity>(Table) {

    }
}

class CollectionEntity(id: EntityID<Long>) : Entity<Long>(id) {
    var category by Table.category
    var creatorId by Table.creatorId
    var public by Table.public

    object Table : LongIdTable("collections") {
        val category = varchar("category", 255)
        val public = bool("public")
        val creatorId = long("creator_id").references(UserEntity.Table.id)

        override val primaryKey: PrimaryKey = PrimaryKey(id)
    }

    companion object : EntityClass<Long, CollectionEntity>(Table) {

    }
}


class FlashcardEntity(id: EntityID<Long>) : Entity<Long>(id) {
    var term by Table.term
    var definition by Table.definition
    var collectionId by Table.collectionId

    object Table : LongIdTable("flashcards") {
        val term = varchar("term", 255)
        val definition = varchar("definition", 255)
        val collectionId = long("collection_id").references(CollectionEntity.Table.id)

        override val primaryKey: PrimaryKey = PrimaryKey(id)
    }


    companion object : EntityClass<Long, FlashcardEntity>(Table) {

    }
}
