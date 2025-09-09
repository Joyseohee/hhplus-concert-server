package kr.hhplus.be.server.infrastructure.message.kafka.topic

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.kafka.topics")
data class TopicProperties(
    val queue: String,
    val reservation: String,
)