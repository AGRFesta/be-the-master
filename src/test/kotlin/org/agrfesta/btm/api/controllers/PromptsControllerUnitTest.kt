package org.agrfesta.btm.api.controllers

import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.verify
import org.agrfesta.btm.api.controllers.config.MessageResponse
import org.agrfesta.btm.api.model.EmbeddingCreationFailure
import org.agrfesta.btm.api.model.TokenCountFailure
import org.agrfesta.btm.api.persistence.EmbeddingsDao
import org.agrfesta.btm.api.persistence.PartiesDao
import org.agrfesta.btm.api.persistence.jdbc.repositories.GlossariesRepository
import org.agrfesta.btm.api.services.ChunksService.Companion.DEFAULT_DISTANCE_LIMIT
import org.agrfesta.btm.api.services.ChunksService.Companion.DEFAULT_EMBEDDINGS_LIMIT
import org.agrfesta.btm.api.services.EmbeddingsProvider
import org.agrfesta.btm.api.services.Tokenizer
import org.agrfesta.test.mothers.aRandomUniqueString
import org.agrfesta.test.mothers.anEmbedding
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PromptsController::class)
@ActiveProfiles("test")
class PromptsControllerUnitTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired @MockkBean private val tokenizer: Tokenizer,
    @Autowired @MockkBean private val embeddingsProvider: EmbeddingsProvider,
    @Autowired @MockkBean private val partiesDao: PartiesDao,
    @Autowired @MockkBean private val embeddingsDao: EmbeddingsDao,
    @Autowired @MockkBean private val glossariesRepository: GlossariesRepository
) {
    private val game = aGame()
    private val topic = aTopic()
    private val language = aSupportedLanguage()
    private val prompt = aRandomUniqueString()

    ///// enhanceBasicPrompt ///////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `enhanceBasicPrompt() Returns 400 when prompt is missing`() {
        val requestJson = aBasicPromptEnhanceRequestJson(prompt = null)
        val responseBody: String = mockMvc.perform(
            post("/prompts/enhance/basic")
                .contentType("application/json")
                .content(requestJson))
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsString

        val response: MessageResponse = objectMapper.readValue(responseBody, MessageResponse::class.java)
        response.message shouldBe "prompt is missing!"
    }

    @TestFactory
    fun `enhanceBasicPrompt() returns 400 when prompt is blank`() = listOf("", " ", "  ", "    ").map {
        dynamicTest(" -> '$it'") {
            val request = aBasicPromptEnhanceRequestJson(prompt = it)
            val responseBody: String = mockMvc.perform(
                post("/prompts/enhance/basic")
                    .contentType("application/json")
                    .content(request))
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

            val response: MessageResponse = objectMapper.readValue(responseBody, MessageResponse::class.java)
            response.message shouldBe "prompt must not be blank!"
        }
    }

    @Test
    fun `enhanceBasicPrompt() Returns 400 when language is missing`() {
        val requestJson = aBasicPromptEnhanceRequestJson(language = null)
        val responseBody: String = mockMvc.perform(
            post("/prompts/enhance/basic")
                .contentType("application/json")
                .content(requestJson))
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsString

        val response: MessageResponse = objectMapper.readValue(responseBody, MessageResponse::class.java)
        response.message shouldBe "language is missing!"
    }

    @Test fun `enhanceBasicPrompt() Returns 400 when language is not valid`() {
        val requestJson = aBasicPromptEnhanceRequestJson(language = aRandomUniqueString())
        val responseBody: String = mockMvc.perform(
            post("/prompts/enhance/basic")
                .contentType("application/json")
                .content(requestJson))
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsString

        val response: MessageResponse = objectMapper.readValue(responseBody, MessageResponse::class.java)
        response.message shouldBe "language is not valid!"
    }

    @TestFactory
    fun `enhanceBasicPrompt()  returns 400 when maxTokens is not valid`() = listOf(Int.MIN_VALUE, -1, 0).map {
        dynamicTest(" -> '$it'") {
            val requestJson = aBasicPromptEnhanceRequestJson(maxTokens = it)
            val responseBody: String = mockMvc.perform(
                post("/prompts/enhance/basic")
                    .contentType("application/json")
                    .content(requestJson))
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

            val response: MessageResponse = objectMapper.readValue(responseBody, MessageResponse::class.java)
            response.message shouldBe "maxTokens must be a positive Int!"
        }
    }

    @TestFactory
    fun `enhanceBasicPrompt() Returns 400 when game is missing`() = listOf(null).map {
        dynamicTest(" -> '$it'") {
            val requestJson = aBasicPromptEnhanceRequestJson(game = it)
            val responseBody: String = mockMvc.perform(
                post("/prompts/enhance/basic")
                    .contentType("application/json")
                    .content(requestJson))
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

            val response: MessageResponse = objectMapper.readValue(responseBody, MessageResponse::class.java)
            response.message shouldBe "game is missing!"
        }
    }

    @Test fun `enhanceBasicPrompt() Returns 400 when game is not valid`() {
        val requestJson = aBasicPromptEnhanceRequestJson(game = aRandomUniqueString())
        val responseBody: String = mockMvc.perform(
            post("/prompts/enhance/basic")
                .contentType("application/json")
                .content(requestJson))
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsString

        val response: MessageResponse = objectMapper.readValue(responseBody, MessageResponse::class.java)
        response.message shouldBe "game is not valid!"
    }

    @TestFactory
    fun `enhanceBasicPrompt() Returns 400 when topic is missing`() = listOf(null).map {
        dynamicTest(" -> '$it'") {
            val requestJson = aBasicPromptEnhanceRequestJson(topic = it)
            val responseBody: String = mockMvc.perform(
                post("/prompts/enhance/basic")
                    .contentType("application/json")
                    .content(requestJson))
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

            val response: MessageResponse = objectMapper.readValue(responseBody, MessageResponse::class.java)
            response.message shouldBe "topic is missing!"
        }
    }

    @Test fun `enhanceBasicPrompt() Returns 400 when topic is not valid`() {
        val requestJson = aBasicPromptEnhanceRequestJson(topic = aRandomUniqueString())
        val responseBody: String = mockMvc.perform(
            post("/prompts/enhance/basic")
                .contentType("application/json")
                .content(requestJson))
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsString

        val response: MessageResponse = objectMapper.readValue(responseBody, MessageResponse::class.java)
        response.message shouldBe "topic is not valid!"
    }

    @Test fun `enhanceBasicPrompt() Returns 500 when fails to create prompt embedding`() {
        val requestJson = aBasicPromptEnhanceRequestJson(
            prompt = prompt,
            game = game.name,
            topic = topic.name,
            language = language.name,
            maxTokens = 8_000
        )
        coEvery { embeddingsProvider.createEmbedding(prompt) } returns EmbeddingCreationFailure.left()
        val responseBody: String = mockMvc.perform(
            post("/prompts/enhance/basic")
                .contentType("application/json")
                .content(requestJson))
            .andExpect(status().isInternalServerError)
            .andReturn().response.contentAsString

        verify(exactly = 0) { embeddingsDao.searchBySimilarity(any(), any(), any(), any(), any(), any()) }
        verify(exactly = 0) { tokenizer.countTokens(any()) }
        val response: MessageResponse = objectMapper.readValue(responseBody, MessageResponse::class.java)
        response.message shouldBe "Unable to create target embedding!"
    }

    @Test fun `enhanceBasicPrompt() Returns 500 when fails to fetch similar chunks`() {
        val requestJson = aBasicPromptEnhanceRequestJson(
            prompt = prompt,
            game = game.name,
            topic = topic.name,
            language = language.name,
            maxTokens = 8_000
        )
        val target = anEmbedding()
        coEvery { embeddingsProvider.createEmbedding(prompt) } returns target.right()
        every { embeddingsDao.searchBySimilarity(target, game, topic, language,
            DEFAULT_EMBEDDINGS_LIMIT,
            DEFAULT_DISTANCE_LIMIT) } throws Exception("search by similarity failure!")
        val responseBody: String = mockMvc.perform(
            post("/prompts/enhance/basic")
                .contentType("application/json")
                .content(requestJson))
            .andExpect(status().isInternalServerError)
            .andReturn().response.contentAsString

        verify(exactly = 0) { tokenizer.countTokens(any()) }
        val response: MessageResponse = objectMapper.readValue(responseBody, MessageResponse::class.java)
        response.message shouldBe "Search by similarity failed!"
    }

    @Test fun `enhanceBasicPrompt() Enhances basic prompt with all chunks when they are less than token limit`() {
        val requestJson = aBasicPromptEnhanceRequestJson(
            prompt = prompt,
            game = game.name,
            topic = topic.name,
            language = language.name,
            maxTokens = 8_000
        )
        val target = anEmbedding()
        val chunkA = aRandomUniqueString()
        val chunkB = aRandomUniqueString()
        val chunkC = aRandomUniqueString()
        val expectedContext = listOf(chunkA to 0.1, chunkB to 0.2, chunkC to 0.3)
        coEvery { embeddingsProvider.createEmbedding(prompt) } returns target.right()
        every { embeddingsDao.searchBySimilarity(target, game, topic, language,
            DEFAULT_EMBEDDINGS_LIMIT,
            DEFAULT_DISTANCE_LIMIT) } returns expectedContext
        every { tokenizer.countTokens(chunkA) } returns 10.right()
        every { tokenizer.countTokens(chunkB) } returns 14.right()
        every { tokenizer.countTokens(chunkC) } returns 104.right()
        val responseBody: String = mockMvc.perform(
            post("/prompts/enhance/basic")
                .contentType("application/json")
                .content(requestJson))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val regex = Regex("""\[(.*?)]""", RegexOption.DOT_MATCHES_ALL)
        val matches = regex.findAll(responseBody).toList()
        val question = matches.getOrNull(0)?.groups?.get(1)?.value
        question shouldBe prompt
        val context = matches.getOrNull(1)?.groups?.get(1)?.value
        context shouldBe "$chunkA\n$chunkB\n$chunkC"

    }

    @Test fun `enhanceBasicPrompt() Enhances basic prompt with partial chunks when they are more than token limit`() {
        val requestJson = aBasicPromptEnhanceRequestJson(
            prompt = prompt,
            game = game.name,
            topic = topic.name,
            language = language.name,
            maxTokens = 600
        )
        val target = anEmbedding()
        val chunkA = aRandomUniqueString()
        val chunkB = aRandomUniqueString()
        val chunkC = aRandomUniqueString()
        val chunkD = aRandomUniqueString()
        val chunkE = aRandomUniqueString()
        val expectedContext = listOf(chunkA to 0.1, chunkB to 0.2, chunkC to 0.3, chunkD to 0.4, chunkE to 0.5)
        coEvery { embeddingsProvider.createEmbedding(prompt) } returns target.right()
        every { embeddingsDao.searchBySimilarity(target, game, topic, language,
            DEFAULT_EMBEDDINGS_LIMIT,
            DEFAULT_DISTANCE_LIMIT) } returns expectedContext
        every { tokenizer.countTokens(chunkA) } returns 200.right()
        every { tokenizer.countTokens(chunkB) } returns 250.right()
        every { tokenizer.countTokens(chunkC) } returns 50.right()
        every { tokenizer.countTokens(chunkD) } returns 400.right()
        every { tokenizer.countTokens(chunkE) } returns 30.right()
        val responseBody: String = mockMvc.perform(
            post("/prompts/enhance/basic")
                .contentType("application/json")
                .content(requestJson))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val regex = Regex("""\[(.*?)]""", RegexOption.DOT_MATCHES_ALL)
        val matches = regex.findAll(responseBody).toList()
        val question = matches.getOrNull(0)?.groups?.get(1)?.value
        question shouldBe prompt
        val context = matches.getOrNull(1)?.groups?.get(1)?.value
        context shouldBe "$chunkA\n$chunkB\n$chunkC"
    }

    @Test fun `enhanceBasicPrompt() Ignores failures counting tokens`() {
        val requestJson = aBasicPromptEnhanceRequestJson(
            prompt = prompt,
            game = game.name,
            topic = topic.name,
            language = language.name,
            maxTokens = 600
        )
        val target = anEmbedding()
        val chunkA = aRandomUniqueString()
        val chunkB = aRandomUniqueString()
        val chunkC = aRandomUniqueString()
        val chunkD = aRandomUniqueString()
        val chunkE = aRandomUniqueString()
        val expectedContext = listOf(chunkA to 0.1, chunkB to 0.2, chunkC to 0.3, chunkD to 0.4, chunkE to 0.5)
        coEvery { embeddingsProvider.createEmbedding(prompt) } returns target.right()
        every { embeddingsDao.searchBySimilarity(target, game, topic, language,
            DEFAULT_EMBEDDINGS_LIMIT,
            DEFAULT_DISTANCE_LIMIT) } returns expectedContext
        every { tokenizer.countTokens(chunkA) } returns 200.right()
        every { tokenizer.countTokens(chunkB) } returns 250.right()
        every { tokenizer.countTokens(chunkC) } returns TokenCountFailure.left()
        every { tokenizer.countTokens(chunkD) } returns 30.right()
        every { tokenizer.countTokens(chunkE) } returns 400.right()
        val responseBody: String = mockMvc.perform(
            post("/prompts/enhance/basic")
                .contentType("application/json")
                .content(requestJson))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val regex = Regex("""\[(.*?)]""", RegexOption.DOT_MATCHES_ALL)
        val matches = regex.findAll(responseBody).toList()
        val question = matches.getOrNull(0)?.groups?.get(1)?.value
        question shouldBe prompt
        val context = matches.getOrNull(1)?.groups?.get(1)?.value
        context shouldBe "$chunkA\n$chunkB\n$chunkD"
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
