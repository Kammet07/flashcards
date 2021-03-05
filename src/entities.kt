package com.kammet.flashcards.backend

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.and
import kotlin.properties.ReadOnlyProperty

val entities = arrayOf(UserEntity.Table, CollectionEntity.Table, FlashcardEntity.Table)

// exposed user entity
class UserEntity(id: EntityID<Long>) : Entity<Long>(id) {
    var username by Table.username
    var password by Table.password
    var mail by Table.mail

    object Table : LongIdTable("users") {
        const val MAX_USERNAME_LENGTH = 20
        const val MAX_PASSWORD_LENGTH = 255
        const val MAX_MAIL_LENGTH = 320

        val username = varchar("username", MAX_USERNAME_LENGTH).uniqueIndex()
        val password = varchar("password", MAX_PASSWORD_LENGTH)
        val mail = varchar("mail", MAX_MAIL_LENGTH).uniqueIndex()

        override val primaryKey: PrimaryKey = PrimaryKey(id)
    }

    companion object Entity : EntityClass<Long, UserEntity>(Table) {
        fun findByUsername(username: String) = find { Table.username eq username }.singleOrNull()
        fun existsByUsername(username: String) = find { Table.username eq username }.empty().not()
        fun existsByMail(mail: String) = find { Table.mail eq mail }.empty().not()

//        TODO: fix with left join
//        fun findCreatedCollections(userId: Long) = find { CollectionEntity.Table.creatorId eq userId }.singleOrNull()
    }
}

fun <T : Comparable<T>, ID : Comparable<ID>> Column<EntityID<T>>.unwrapped() = ReadOnlyProperty<Entity<ID>, T> { t, p ->
    with(t) {
        getValue(t, p).value
    }
}

// exposed collection entity
class CollectionEntity(id: EntityID<Long>) : Entity<Long>(id) {
    var category by Table.category
    val creatorId by Table.creator.unwrapped()
    var public by Table.public
    var creator by UserEntity referencedOn Table.creator
    val cards by FlashcardEntity referrersOn FlashcardEntity.Table.collection


    object Table : LongIdTable("collections") {
        const val MAX_CATEGORY_LENGTH = 255

        val category = varchar("category", MAX_CATEGORY_LENGTH)
        val public = bool("public")
        val creator = reference("creator_id", UserEntity.Table)


        override val primaryKey: PrimaryKey = PrimaryKey(id)
    }

    companion object Entity : EntityClass<Long, CollectionEntity>(Table) {
        fun findByCreatorId(creatorId: Long) = find { Table.creator eq creatorId }.toList()
        fun findPublic() = find { Table.public eq true }.toList()
        fun findUserPrivate(creatorId: Long) =
            find { (Table.public eq false).and(Table.creator eq creatorId) }.toList()

        fun findUserPublic(creatorId: Long) =
            find { (Table.public eq true).and(Table.creator eq creatorId) }.toList()

        fun wasCreatedByUser(id: Long, creatorId: Long) =
            find { (Table.id eq id).and(Table.creator eq creatorId) }.empty().not()
    }
}

// exposed flashcard entity
class FlashcardEntity(id: EntityID<Long>) : Entity<Long>(id) {
    var term by Table.term
    var definition by Table.definition
    var collection by CollectionEntity referencedOn Table.collection

    object Table : LongIdTable("flashcards") {
        const val MAX_TERM_LENGTH = 255
        const val MAX_DEFINITION_LENGTH = 255

        val term = varchar("term", MAX_TERM_LENGTH)
        val definition = varchar("definition", MAX_DEFINITION_LENGTH)

        // val collectionId = long("collection_id").references(CollectionEntity.Table.id)
        val collection = reference("collection_id", CollectionEntity.Table)

        override val primaryKey: PrimaryKey = PrimaryKey(id)
    }

    companion object : EntityClass<Long, FlashcardEntity>(Table) {
        fun findByCollectionId(collectionId: Long) = find { Table.collection eq collectionId }.toList()
    }
}
