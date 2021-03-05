package com.kammet.flashcards.backend.api

import com.kammet.flashcards.backend.CollectionEntity
import com.kammet.flashcards.backend.FlashcardEntity
import com.kammet.flashcards.backend.forbiddenIf
import com.kammet.flashcards.backend.validate
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

@Location("/flashcard")
class FlashcardLocation {
    @Location("/{collectionId}/{flashcardId}")
    data class Detail(val collectionId: Long, val flashcardId: Long)
}


interface IFlashcardModel {
    val id: Long
    val term: String
    val definition: String
    val collectionId: Long

    data class Create(
        override val id: Long,
        @Size(min = 1, max = FlashcardEntity.Table.MAX_TEXT_LENGTH)
        override val term: String,
        @Size(min = 1, max = FlashcardEntity.Table.MAX_TEXT_LENGTH)
        override val definition: String,
        override val collectionId: Long
    ) : IFlashcardModel

    data class Edit(
        override val id: Long,
        @Size(min = 1, max = FlashcardEntity.Table.MAX_TEXT_LENGTH)
        override val term: String,
        @Size(min = 1, max = FlashcardEntity.Table.MAX_TEXT_LENGTH)
        override val definition: String,
        override val collectionId: Long
    ) : IFlashcardModel
}

data class FlashcardViewModel(val id: Long, val term: String, val definition: String, val collectionId: Long)

fun FlashcardEntity.asViewModel() = FlashcardViewModel(id.value, term, definition, collectionId)

fun FlashcardEntity.valuesFrom(model: IFlashcardModel) {
    term = model.term
    definition = model.definition
    collection = model.collectionId.let { CollectionEntity.findById(it)!! }
}

fun Route.flashcardRoutes() {
    get<CollectionLocation.Detail> { location ->
//        println(transaction { FlashcardEntity.findByCollectionId(location.collectionId) }.map { it.asViewModel() })
        when (val collection = transaction { CollectionEntity.findById(location.collectionId) }) {
            null -> {
                call.response.status(HttpStatusCode.NotFound)
                call.respond("entity not found")
            }
            else -> when {
                collection.public -> call.respond(transaction { FlashcardEntity.findByCollectionId(location.collectionId) }.map { it.asViewModel() })
                else -> when (call.identity?.id) {
                    collection.creatorId -> transaction { FlashcardEntity.findByCollectionId(location.collectionId) }.map { it.asViewModel() }
                    else -> {
                        call.response.status(HttpStatusCode.Forbidden)
                        call.respond("forbidden")
                    }
                }
            }
        }
    }

    authenticate {
        post<CollectionLocation.Detail> { location ->
            val model = call.receive<IFlashcardModel.Create>().also { it.validate() }
            when (val collection = transaction { CollectionEntity.findById(location.collectionId) }) {
                null -> {
                    call.response.status(HttpStatusCode.NotFound)
                    call.respond("entity not found")
                }
                else -> {
                    val flashcard = transaction {
                        when (call.identity?.id) {
                            collection.creatorId -> FlashcardEntity.new { valuesFrom(model) }
                            else -> null
                        }
                    }
                    when (flashcard) {
                        null -> {
                            call.response.status(HttpStatusCode.Forbidden)
                            call.respond("wrong creator id")
                        }
                        else -> {
                            call.response.status(HttpStatusCode.Created)
                            call.respond(flashcard.asViewModel())
                        }
                    }
                }
            }
        }

        put<FlashcardLocation.Detail> { location ->
            val model = call.receive<IFlashcardModel.Edit>().also { it.validate() }
            when (val collection = transaction { CollectionEntity.findById(location.collectionId) }) {
                null -> {
                    call.response.status(HttpStatusCode.NotFound)
                    call.respond("entity not found")
                }
                else -> {
                    val flashcard = transaction {
                        when (call.identity?.id) {
                            collection.creatorId -> FlashcardEntity.findById(model.id)
                                ?.also { it.valuesFrom(model) }
                            else -> null
                        }
                    }
                    when (flashcard) {
                        null -> {
                            call.response.status(HttpStatusCode.Forbidden)
                            call.respond("wrong creator id")
                        }
                        else -> {
                            call.response.status(HttpStatusCode.Created)
                            call.respond(flashcard.asViewModel())
                        }
                    }
                }
            }
        }

        delete<FlashcardLocation.Detail> { location ->
            when (transaction { FlashcardEntity.findById(location.flashcardId) }) {
                null -> {
                    call.response.status(HttpStatusCode.NotFound)
                    call.respond("entity not found")
                }
                else -> {
                    val collection = transaction { CollectionEntity.findById(location.collectionId) }
                    call.forbiddenIf { call.identity?.id != collection?.creatorId }
                    transaction {
                        FlashcardEntity.Table.deleteIgnoreWhere { FlashcardEntity.Table.id eq location.flashcardId }
                    }
                    call.respond("removed")
                }
            }
        }
    }
}
