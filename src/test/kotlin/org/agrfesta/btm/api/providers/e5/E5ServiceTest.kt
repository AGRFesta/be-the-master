package org.agrfesta.btm.api.providers.e5

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.agrfesta.btm.api.model.Embedding
import org.agrfesta.btm.api.model.EmbeddingCreationFailure
import org.agrfesta.test.mothers.aRandomUniqueString
import org.agrfesta.test.mothers.anEmbedding
import org.junit.jupiter.api.Test
import kotlin.random.Random

class E5ServiceTest{
    private val mapper = jacksonObjectMapper()
    private val baseUrl = "http://${aRandomUniqueString()}"
    private val text = aRandomUniqueString()

    ///// countTokens() ////////////////////////////////////////////////////////////////////////////////////////////////

    @Test fun `countTokens() returns number of tokens of input string`() {
        val tokens = Random.nextInt(0, 500)
        val engine = MockEngine { request ->
            request.method shouldBe HttpMethod.Post
            request.url.toString() shouldBe "$baseUrl/count-tokens"
            val requestBody = request.body.toByteArray().decodeToString()
            val jsonNodeBody: JsonNode = mapper.readTree(requestBody)
            val requestText: String = jsonNodeBody.at("/sentences/0").asText()
            requestText shouldBe text
            respond(
                content = createCountTokensResponse(text, tokens),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = E5Client(baseUrl, engine)
        val sut = E5Service(client)

        val result = runBlocking { sut.countTokens(text) }

        result shouldBeRight tokens
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// createEmbedding() ////////////////////////////////////////////////////////////////////////////////////////////

    @Test fun `createEmbedding() returns embedding of input string`() {
        val embedding = anEmbedding()
        val engine = MockEngine { request ->
            request.method shouldBe HttpMethod.Post
            request.url.toString() shouldBe "$baseUrl/embed"
            val requestBody = request.body.toByteArray().decodeToString()
            val jsonNodeBody: JsonNode = mapper.readTree(requestBody)
            val requestText: String = jsonNodeBody.at("/sentences/0").asText()
            requestText shouldBe text
            respond(
                content = createEmbedResponse(embedding),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = E5Client(baseUrl, engine)
        val sut = E5Service(client)

        val result = runBlocking { sut.createEmbedding(text) }

        result shouldBeRight embedding
    }

    @Test fun `createEmbedding() returns EmbeddingCreationFailure when receives unexpected response`() {
        val engine = MockEngine { request ->
            request.method shouldBe HttpMethod.Post
            request.url.toString() shouldBe "$baseUrl/embed"
            val requestBody = request.body.toByteArray().decodeToString()
            val jsonNodeBody: JsonNode = mapper.readTree(requestBody)
            val requestText: String = jsonNodeBody.at("/sentences/0").asText()
            requestText shouldBe text
            respond(
                content = "{}", // unexpected empty json
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = E5Client(baseUrl, engine)
        val sut = E5Service(client)

        val result = runBlocking { sut.createEmbedding(text) }

        result shouldBeLeft EmbeddingCreationFailure
    }

    @Test fun `createEmbedding() returns EmbeddingCreationFailure when creation fails`() {
        val engine = MockEngine { request ->
            request.method shouldBe HttpMethod.Post
            request.url.toString() shouldBe "$baseUrl/embed"
            val requestBody = request.body.toByteArray().decodeToString()
            val jsonNodeBody: JsonNode = mapper.readTree(requestBody)
            val requestText: String = jsonNodeBody.at("/sentences/0").asText()
            requestText shouldBe text
            respond(
                content = "{}", // don't care
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = E5Client(baseUrl, engine)
        val sut = E5Service(client)

        val result = runBlocking { sut.createEmbedding(text) }

        result shouldBeLeft EmbeddingCreationFailure
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun createCountTokensResponse(text:String, tokens: Int): String = """
        {
          "token_counts": [
            {
              "sentence": "$text",
              "token_count": $tokens
            }
          ]
        }
    """.trimIndent()

    private fun createEmbedResponse(embedding: Embedding): String = """
        {
          "vectors": [${embedding.joinToString(prefix = "[", postfix = "]")}]
        }
    """.trimIndent()
}
