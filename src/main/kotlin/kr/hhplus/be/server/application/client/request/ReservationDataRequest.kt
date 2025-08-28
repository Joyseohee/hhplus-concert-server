package kr.hhplus.be.server.application.client.request

data class ReservationDataRequest(
	val reservationId: Long,
	val concertId: Long,
	val userId: Long,
) {
}