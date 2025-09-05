package kr.hhplus.be.server

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@ActiveProfiles("test")
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

        @Container
        @JvmStatic
        val image = DockerImageName.parse("apache/kafka:3.8.0") // 예시: 3.6.x~3.8.x 아무거나 안정된 것
        val kafka = KafkaContainer(image)
            .apply {
                start()  // Kafka 컨테이너 시작
            }

        @JvmStatic
        @DynamicPropertySource
        fun overrideProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { mysql.jdbcUrl }
            registry.add("spring.datasource.username") { mysql.username }
            registry.add("spring.datasource.password") { mysql.password }
            registry.add("spring.data.redis.host") { redis.host }
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }
        }
    }

    init {
        extension(SpringExtension)
    }
}
