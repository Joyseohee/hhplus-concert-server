package kr.hhplus.be.server.infrastructure.message.kafka.dto

data class ConfirmReservationMessage(
	val reservationId: Long = 0L,
	val concertId: Long = 0L,
	val userId: Long = 0L
)