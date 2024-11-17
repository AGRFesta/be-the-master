package org.agrfesta.btm.api.providers.openai

import arrow.core.Either
import org.agrfesta.btm.api.model.Embedding
import org.agrfesta.btm.api.model.EmbeddingCreationFailure
import org.agrfesta.btm.api.services.EmbeddingsService
import org.springframework.stereotype.Service

@Service
class OpenAiService(
    private val openAiClient: OpenAiClient
): EmbeddingsService {

    override suspend fun createEmbedding(text: String): Either<EmbeddingCreationFailure, Embedding> =
        openAiClient.createEmbedding(text)
            .map {
                //TODO print usage
                it.embedding
            }

}
