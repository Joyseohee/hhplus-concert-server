package kr.hhplus.be.server.application

import kr.hhplus.be.server.domain.repository.ReservationRepository
import kr.hhplus.be.server.infrastructure.message.kafka.dto.ConfirmReservationMessage
import kr.hhplus.be.server.infrastructure.message.kafka.producer.ReservationProducer
import org.springframework.stereotype.Service

@Service
class SendReservationDataUseCase(
	private val reservationProducer: ReservationProducer,
	private val reservationRepository: ReservationRepository
) {
	fun execute(reservationId: Long, userId: Long) {
		val reservation = reservationRepository.findByIdOrElseThrow(reservationId)

		reservationProducer.send(
			ConfirmReservationMessage(
				userId = userId,
				reservationId = reservation!!.reservationId!!,
				concertId = reservation.concertId
			)
		)
	}

}