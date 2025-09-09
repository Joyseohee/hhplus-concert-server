package kr.hhplus.be.server.infrastructure.message.kafka.listener

import kr.hhplus.be.server.infrastructure.message.kafka.dto.PendingMessage
import kr.hhplus.be.server.infrastructure.message.kafka.dto.QueueEnteredMessage
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentLinkedQueue

@Component
class QueueListener{

    private val buffer: ConcurrentLinkedQueue<PendingMessage<QueueEnteredMessage>> = ConcurrentLinkedQueue()

    @KafkaListener(
        topics = ["\${app.kafka.topics.queue}"],
        groupId = "concert-queue",
        concurrency = "1"   // 순차 처리 위해 1로 설정
    )
    fun consumeQueue(message: QueueEnteredMessage, ack: Acknowledgment) {
        try {
            buffer.offer(PendingMessage(message, ack))
        } catch (ex: Exception) {
            println("Failed queue consume: ${ex.message}")
        }
    }

    fun poll(): PendingMessage<QueueEnteredMessage>? = buffer.poll()

}
