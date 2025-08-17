package kr.hhplus.be.server

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

abstract class SpringIntegrationTestSupport {

    companion object {
        private val mysql: MySQLContainer<*> = MySQLContainer(DockerImageName.parse("mysql:8.0")).apply {
            withDatabaseName("hhplus")
            withUsername("application")
            withPassword("application")
            withReuse(true) // 속도 향상 (optional, ~/.testcontainers.properties 필요)
            start()
        }

        private val redis = GenericContainer<Nothing>(DockerImageName.parse("redis:7.2")).apply {
            withExposedPorts(6379)
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun overrideProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { mysql.jdbcUrl }
            registry.add("spring.datasource.username") { mysql.username }
            registry.add("spring.datasource.password") { mysql.password }
            registry.add("spring.datasource.driver-class-name") { "com.mysql.cj.jdbc.Driver" }
            registry.add("spring.data.redis.host") { KotestIntegrationSpec.Companion.redis.host }
            registry.add("spring.data.redis.port") { KotestIntegrationSpec.Companion.redis.getMappedPort(6379) }
        }
    }
}