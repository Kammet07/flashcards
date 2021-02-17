package com.kammet.flashcards.backend.api

import com.kammet.flashcards.backend.CollectionEntity
import com.kammet.flashcards.backend.validate
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import javax.validation.constraints.Size

@KtorExperimentalLocationsAPI
@Location("/collection")
class CollectionLocation {
    @Location("/{collectionId}")

    data class Detail(val collectionId: Long, val collectionLocation: CollectionLocation)
}

@KtorExperimentalLocationsAPI
@Location("/user/{creatorId}/collections")
data class UserCollections(val creatorId: Long)

interface ICollectionModel {
    val id: Long?
    val category: String
    val public: Boolean
    val creatorId: Long

    data class Create(
        override val id: Long?,
        @Size(min = 1, max = CollectionEntity.Table.MAX_CATEGORY_LENGTH)
        override val category: String,
        override val public: Boolean,
        override val creatorId: Long
    ) : ICollectionModel

    data class Edit(
        override val id: Long,
        @Size(min = 1, max = CollectionEntity.Table.MAX_CATEGORY_LENGTH)
        override val category: String,
        override val public: Boolean,
        override val creatorId: Long
    ) : ICollectionModel

}

data class CollectionViewModel(val id: Long, val category: String, val public: Boolean, val creatorId: Long)

fun CollectionEntity.asViewModel() = CollectionViewModel(id.value, category, public, creatorId)

fun CollectionEntity.valuesFrom(model: ICollectionModel) {
    category = model.category
    public = model.public
    creatorId = model.creatorId
}

@KtorExperimentalLocationsAPI
fun Route.collectionRoutes() {
    authenticate {
        /**
         * create collection
         *
         * TODO: instead of getting id, define it trough identity
         */
        post<CollectionLocation> {
            val model = call.receive<ICollectionModel.Create>().also { it.validate() }
            val collection = transaction {
                when {
                    call.identity?.id != model.creatorId -> null
                    else -> CollectionEntity.new { valuesFrom(model) }
                }
            }
            when (collection) {
                null -> {
                    call.response.status(HttpStatusCode.Forbidden)
                    call.respond("wrong creator id")
                }
                else -> {
                    call.response.status(HttpStatusCode.Created)
                    call.respond(collection.asViewModel())
                }
            }
        }

        /**
         * edit collection
         */
        put<CollectionLocation.Detail> {
            val model = call.receive<ICollectionModel.Edit>().also { it.validate() }
            val collection = transaction {
                when {
                    call.identity!!.id == model.creatorId && CollectionEntity.wasCreatedByUser(
                        model.id,
                        model.creatorId
                    ) -> CollectionEntity.findById(model.id)?.also { it.valuesFrom(model) }
                    else -> null
                }
            }
            when (collection) {
                null -> {
                    call.response.status(HttpStatusCode.Conflict)
                    call.respond("category does not exist or user is not creator")
                }
                else -> call.respond(collection.asViewModel())
            }
        }

        /**
         * delete collection with all connected flashcards
         *
         * TODO: implement
         */
        delete<CollectionLocation.Detail> {

        }
    }

    /**
     * get all collections
     */
    get<CollectionLocation> {
        call.respond(transaction {
            CollectionEntity.findPublic().map { it.asViewModel() }
        })
    }

    get<UserCollections> { location ->
        when (call.identity?.id) {
            location.creatorId -> call.respond(transaction {
                CollectionEntity.findByCreatorId(location.creatorId).map { it.asViewModel() }
            })
            else ->
                call.respond(transaction {
                    CollectionEntity.findUserPublic(location.creatorId).map { it.asViewModel() }
                })
        }
    }


}