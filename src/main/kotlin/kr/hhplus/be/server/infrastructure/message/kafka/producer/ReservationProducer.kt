package kr.hhplus.be.server.infrastructure.message.kafka.producer

import kr.hhplus.be.server.infrastructure.message.kafka.dto.ConfirmReservationMessage
import kr.hhplus.be.server.infrastructure.message.kafka.topic.TopicProperties
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class ReservationProducer(
	private val kafkaTemplate: KafkaTemplate<Any, ConfirmReservationMessage>,
	private val topics: TopicProperties
) {
	fun send(payload: ConfirmReservationMessage) =

		kafkaTemplate.send(topics.reservation, payload)
			.whenComplete { result, ex ->
				if (ex == null) {
					println("✅ Reservation sent: partition=${result.recordMetadata.partition()}, offset=${result.recordMetadata.offset()}")
				} else {
					println("❌ Reservation send failed: ${ex.message}")
				}
			}
}
