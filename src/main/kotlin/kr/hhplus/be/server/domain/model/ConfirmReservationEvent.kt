package kr.hhplus.be.server.domain.model

data class ConfirmReservationEvent(
	val reservationId: Long,
	val userId: Long
) : DomainEvent
