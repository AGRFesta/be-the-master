package org.agrfesta.btm.api.controllers

import org.agrfesta.btm.api.model.Game
import org.agrfesta.btm.api.model.Chunk
import org.agrfesta.btm.api.model.SupportedLanguage
import org.agrfesta.btm.api.model.Topic
import org.agrfesta.btm.api.model.Translation
import org.agrfesta.test.mothers.aRandomUniqueString
import java.util.*
import kotlin.collections.Collection

fun aGame() = Game.entries.random()
fun aTopic() = Topic.entries.random()
fun aLanguage() = SupportedLanguage.entries.random()
fun aSupportedLanguage() = SupportedLanguage.entries.random()

fun aChunk(
    id: UUID = UUID.randomUUID(),
    game: Game = aGame(),
    topic: Topic = aTopic(),
    translations: Set<Translation> = emptySet()
) = Chunk(id, game, topic, translations)

fun aChunksCreationRequestJson(
    game: String? = aGame().name,
    topic: String? = aTopic().name,
    language: String? = aLanguage().name,
    texts: List<String>? = List(3) { aRandomUniqueString() },
    embed: Boolean? = true
): String {
    val properties = buildList {
        game?.let { add(""""game": "$it"""") }
        topic?.let { add(""""topic": "$topic"""") }
        language?.let { add(""""language": "$language"""") }
        texts?.let { add(""""texts": ${texts.toJsonStringArray()}""") }
        embed?.let { add(""""embed": $embed""") }
    }

    return properties.joinToString(
        separator = ",\n    ",
        prefix = "{\n    ",
        postfix = "\n}"
    )
}

fun aChunkTranslationsPatchRequestJson(
    text: String? = aRandomUniqueString(),
    language: String? = aLanguage().name,
    inBatch: Boolean? = true
): String {
    val properties = buildList {
        text?.let { add(""""text": "$it"""") }
        language?.let { add(""""language": "$language"""") }
        inBatch?.let { add(""""embed": $inBatch""") }
    }

    return properties.joinToString(
        separator = ",\n    ",
        prefix = "{\n    ",
        postfix = "\n}"
    )
}

fun Collection<String>.toJsonStringArray(): String = joinToString(
    prefix = "[",
    postfix = "]",
    separator = ","
) { "\"${it}\"" }

fun aChunkTranslationsPatchRequest(
    text: String = aRandomUniqueString(),
    language: SupportedLanguage = aLanguage(),
    inBatch: Boolean = false
) = ChunkTranslationPatchRequest(text, language, inBatch)

fun ChunkTranslationPatchRequest.toJsonString() = """
        {
            "text": "$text",
            "language": "$language",
            "inBatch": $inBatch
        }
    """.trimIndent()

fun aChunkSearchBySimilarityRequest(
    game: Game = aGame(),
    topic: Topic = aTopic(),
    language: SupportedLanguage = aLanguage(),
    text: String = aRandomUniqueString(),
    embeddingsLimit: Int? = null,
    distanceLimit: Double? = null
) = ChunkSearchBySimilarityRequest(game, topic, text, language, embeddingsLimit, distanceLimit)

fun aChunkSearchBySimilarityRequestJson(
    game: String? = aGame().name,
    topic: String? = aTopic().name,
    language: String? = aLanguage().name,
    text: String? = aRandomUniqueString(),
    embeddingsLimit: Int? = null,
    distanceLimit: Double? = null
): String {
    val properties = buildList {
        game?.let { add(""""game": "$it"""") }
        topic?.let { add(""""topic": "$topic"""") }
        language?.let { add(""""language": "$language"""") }
        text?.let { add(""""text": "$text"""") }
        embeddingsLimit?.let { add(""""embeddingsLimit": $embeddingsLimit""") }
        distanceLimit?.let { add(""""distanceLimit": $distanceLimit""") }
    }

    return properties.joinToString(
        separator = ",\n    ",
        prefix = "{\n    ",
        postfix = "\n}"
    )
}

fun ChunkSearchBySimilarityRequest.toJsonString() =
    aChunkSearchBySimilarityRequestJson(game.name, topic.name, language.name, text)
