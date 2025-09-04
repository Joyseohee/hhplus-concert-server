package kr.hhplus.be.server.config

import org.testcontainers.utility.DockerImageName
import org.testcontainers.kafka.KafkaContainer

object SharedKafka {
    val kafka: KafkaContainer by lazy {
        KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"))
            .withReuse(true)
            .also { it.start() }
    }
}
