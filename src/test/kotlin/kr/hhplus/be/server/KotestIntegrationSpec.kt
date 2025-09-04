package kr.hhplus.be.server

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import kr.hhplus.be.server.config.SharedKafka
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
abstract class KotestIntegrationSpec(
    spec: BehaviorSpec.() -> Unit
) : BehaviorSpec(spec) {

    companion object {
        val mysql = MySQLContainer(DockerImageName.parse("mysql:8.0")).apply {
            withDatabaseName("hhplus")
            withUsername("application")
            withPassword("application")
            start()
        }

        val redis = GenericContainer<Nothing>(DockerImageName.parse("redis:7.2")).apply {
            withExposedPorts(6379)
            start()
        }

        @JvmStatic
        @BeforeAll
        fun startKafka() {
            SharedKafka.kafka // lazy start
        }

        @JvmStatic
        @DynamicPropertySource
        fun config(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { mysql.jdbcUrl }
            registry.add("spring.datasource.username") { mysql.username }
            registry.add("spring.datasource.password") { mysql.password }
            registry.add("spring.data.redis.host") { redis.host }
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
            registry.add("spring.kafka.bootstrap-servers") { SharedKafka.kafka.bootstrapServers }
        }
    }

    init {
        extension(SpringExtension)
    }
}
