package kr.hhplus.be.server.infrastructure.message.kafka.producer

import kr.hhplus.be.server.infrastructure.message.kafka.dto.QueueEnteredMessage
import kr.hhplus.be.server.infrastructure.message.kafka.topic.TopicProperties
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class QueueProducer(
    private val kafkaTemplate: KafkaTemplate<Any, QueueEnteredMessage>,
    private val topics: TopicProperties
) {
    fun send(payload: QueueEnteredMessage) =
        kafkaTemplate.send(topics.queue,payload)
}
