package com.kammet.flashcards.backend.api

import com.kammet.flashcards.backend.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.deleteIgnoreWhere
import org.jetbrains.exposed.sql.transactions.transaction
import javax.validation.constraints.Size

@Location("/collection")
class CollectionLocation {
    @Location("/{collectionId}")
    data class Detail(val collectionId: Long, val collectionLocation: CollectionLocation)
}

@Location("/user/{creatorId}/collections")
data class UserCollections(val creatorId: Long)

interface ICollectionModel {
    val category: String
    val public: Boolean
    val creatorId: Long

    data class Create(
        @Size(min = 1, max = CollectionEntity.Table.MAX_CATEGORY_LENGTH)
        override val category: String,
        override val public: Boolean,
        override val creatorId: Long
    ) : ICollectionModel

    data class Edit(
        @Size(min = 1, max = CollectionEntity.Table.MAX_CATEGORY_LENGTH)
        override val category: String,
        override val public: Boolean,
        override val creatorId: Long
    ) : ICollectionModel

}

data class CollectionViewModel(val id: Long, val category: String, val public: Boolean, val creator: UserViewModel?)

fun CollectionEntity.asViewModel() = CollectionViewModel(id.value, category, public, creator.asViewModel())

fun CollectionEntity.valuesFrom(model: ICollectionModel, creator: UserEntity) {
    category = model.category
    public = model.public
    this.creator = creator
}

fun Route.collectionRoutes() {
    authenticate {
        /**
         * create collection
         */
        post<CollectionLocation> {
            val model = call.receive<ICollectionModel.Create>().also { it.validate() }
            val collection = transaction {
                when {
                    call.identity?.id != model.creatorId -> null
                    else -> CollectionEntity.new { valuesFrom(model, call.identity!!.asEntity()) }.asViewModel()
                }
            }
            when (collection) {
                null -> {
                    call.response.status(HttpStatusCode.Forbidden)
                    call.respond("wrong creator id")
                }
                else -> {
                    call.response.status(HttpStatusCode.Created)
                    call.respond(collection)
                }
            }
        }

        /**
         * edit collection
         */
        put<CollectionLocation.Detail> { location ->
            val model = call.receive<ICollectionModel.Edit>().also { it.validate() }
            val collection = transaction {
                when {
                    call.identity!!.id == model.creatorId && CollectionEntity.wasCreatedByUser(
                        location.collectionId,
                        model.creatorId
                    ) -> CollectionEntity.findById(location.collectionId)
                        ?.also { it.valuesFrom(model, call.identity!!.asEntity()) }?.asViewModel()
                    else -> null
                }
            }
            when (collection) {
                null -> {
                    call.response.status(HttpStatusCode.Conflict)
                    call.respond("category does not exist or user is not creator")
                }
                else -> call.respond(collection)
            }
        }

        /**
         * delete collection with all connected flashcards
         */
        delete<CollectionLocation.Detail> { location ->
            when (val collection = transaction { CollectionEntity.findById(location.collectionId) }) {
                null -> {
                    call.response.status(HttpStatusCode.NotFound)
                    call.respond("entity not found")
                }
                else -> {
                    call.forbiddenIf { call.identity?.id != collection.creatorId }
                    transaction {
                        FlashcardEntity.Table.deleteIgnoreWhere { FlashcardEntity.Table.collection eq location.collectionId }
                    }
                    transaction {
                        CollectionEntity.Table.deleteIgnoreWhere { CollectionEntity.Table.id eq location.collectionId }
                    }
                    call.respond("removed")
                }
            }
        }
    }

    /**
     * get all collections
     */
    get<CollectionLocation> {
        when (call.identity?.id) {
            null -> {
                call.respond(transaction {
                    CollectionEntity.findPublic()
                        .map { it.asViewModel() }
                        .toList()
                })
            }
            else -> {
                call.respond(transaction {
                    CollectionEntity.findPublicAndCreatedByUser(call.identity!!.id)
                        .map { it.asViewModel() }
                        .toList()
                })
            }
        }
    }

    get<UserCollections> { location ->
        when (call.identity?.id) {
            location.creatorId -> call.respond(transaction {
                CollectionEntity.findByCreatorId(location.creatorId)
                    .map { it.asViewModel() }
            })
            else ->
                call.respond(transaction {
                    CollectionEntity.findUserPublic(location.creatorId).map { it.asViewModel() }
                })
        }
    }
}
