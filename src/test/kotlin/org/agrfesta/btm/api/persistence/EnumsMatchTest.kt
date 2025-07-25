package org.agrfesta.btm.api.persistence

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.restassured.RestAssured
import org.agrfesta.btm.api.model.EmbeddingStatus
import org.agrfesta.btm.api.model.Game
import org.agrfesta.btm.api.model.SupportedLanguage
import org.agrfesta.btm.api.model.Topic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class EnumsMatchTest(
    @Autowired private val jdbcTemplate: JdbcTemplate
) {
    companion object {
        @Container
        @ServiceConnection
        val postgres: PostgreSQLContainer<*> = DockerImageName.parse("pgvector/pgvector:pg16")
            .asCompatibleSubstituteFor("postgres")
            .let { PostgreSQLContainer(it) }
    }

    @LocalServerPort
    private val port: Int? = null

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost:$port"
    }

    @Test fun `verify PostgreSQL game_enum matches Kotlin Game enum`() {
        val postgresEnumValues: List<String> = jdbcTemplate.query(
            "SELECT unnest(enum_range(NULL::game_enum))::text"
        ) { rs, _ -> rs.getString(1) }

        val kotlinEnumValues: List<String> = Game.entries.map { it.name }

        kotlinEnumValues.shouldContainExactlyInAnyOrder(postgresEnumValues)
    }

    @Test fun `verify PostgreSQL topic_enum matches Kotlin Topic enum`() {
        val postgresEnumValues: List<String> = jdbcTemplate.query(
            "SELECT unnest(enum_range(NULL::topic_enum))::text"
        ) { rs, _ -> rs.getString(1) }

        val kotlinEnumValues: List<String> = Topic.entries.map { it.name }

        kotlinEnumValues.shouldContainExactlyInAnyOrder(postgresEnumValues)
    }

    @Test fun `verify PostgreSQL embedding_status_enum matches Kotlin Topic enum`() {
        val postgresEnumValues: List<String> = jdbcTemplate.query(
            "SELECT unnest(enum_range(NULL::embedding_status_enum))::text"
        ) { rs, _ -> rs.getString(1) }

        val kotlinEnumValues: List<String> = EmbeddingStatus.entries.map { it.name }

        kotlinEnumValues.shouldContainExactlyInAnyOrder(postgresEnumValues)
    }

    @Test fun `verify PostgreSQL supported_language_enum matches Kotlin Topic enum`() {
        val postgresEnumValues: List<String> = jdbcTemplate.query(
            "SELECT unnest(enum_range(NULL::supported_language_enum))::text"
        ) { rs, _ -> rs.getString(1) }

        val kotlinEnumValues: List<String> = SupportedLanguage.entries.map { it.name }

        kotlinEnumValues.shouldContainExactlyInAnyOrder(postgresEnumValues)
    }

}
