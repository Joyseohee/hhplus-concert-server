package kr.hhplus.be.server.infrastructure.message.kafka.listener

import kr.hhplus.be.server.application.client.ReservationDataClient
import kr.hhplus.be.server.application.client.request.ReservationDataRequest
import kr.hhplus.be.server.application.client.response.ReservationDataResponse
import kr.hhplus.be.server.infrastructure.message.kafka.dto.ConfirmReservationMessage
import kr.hhplus.be.server.infrastructure.message.kafka.topic.TopicProperties
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class ReservationListener(
    private val reservationDataClient: ReservationDataClient,
    private val topics: TopicProperties
) {
    @KafkaListener(
        topics = ["\${app.kafka.topics.reservation}"],
        groupId = "concert-reservation",
        concurrency = "3" // 파티션 수와 동일하게 설정하여 병렬 처리
    )
    fun consumeReservation(message: ConfirmReservationMessage, ack: Acknowledgment) {
        try {
            val response = reservationDataClient.execute(
                ReservationDataRequest(
                    reservationId = message.reservationId,
                    concertId = message.concertId,
                    userId = message.userId,
                )
            )

            if (response == null || response.resultType == ReservationDataResponse.ResultType.FAILURE) {
                throw IllegalStateException("예약 데이터 전송에 실패했습니다. reservationId=${message.reservationId}, userId=${message.userId}")
            }
            ack.acknowledge()
        } catch (ex: Exception) {
            println("❌ Failed reservation consume: ${ex.message}")
        }
    }
}
