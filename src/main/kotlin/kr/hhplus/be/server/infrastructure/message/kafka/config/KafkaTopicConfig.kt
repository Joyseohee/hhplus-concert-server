package kr.hhplus.be.server.infrastructure.message.kafka.config

import jakarta.annotation.PostConstruct
import kr.hhplus.be.server.infrastructure.message.kafka.topic.TopicProperties
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaConnectionDetails
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.stereotype.Component

@Configuration
@EnableConfigurationProperties(TopicProperties::class)
class KafkaTopicConfig(
    private val topics: TopicProperties
) {
    // 실습용으로 토픽 동적 생성 -> 운영일 경우 토픽을 미리 생성
    @Bean
    @ConditionalOnProperty(name = ["app.kafka.autocreate"], havingValue = "true", matchIfMissing = true)
    fun queueTopic(): NewTopic = TopicBuilder.name(topics.queue).partitions(1).replicas(1).build()

    @Bean
    @ConditionalOnProperty(name = ["app.kafka.autocreate"], havingValue = "true", matchIfMissing = true)
    fun reserveTopic(): NewTopic = TopicBuilder.name(topics.reservation).partitions(3).replicas(1).build()

}

@Component
class KafkaBootstrapLogger(private val props: KafkaProperties,  private val details: KafkaConnectionDetails) {
    @PostConstruct fun log() {
        println(">>> bootstrapServers = ${props.bootstrapServers}")
        println(">>> KafkaProperties.bootstrapServers = ${props.bootstrapServers}")
        println(">>> ConnectionDetails.bootstrap     = ${details.bootstrapServers}")
        println(">>> ConnectionDetails.admin         = ${details.adminBootstrapServers}")
        println(">>> ConnectionDetails.producer      = ${details.producerBootstrapServers}")
        println(">>> ConnectionDetails.consumer      = ${details.consumerBootstrapServers}")
    }

}
