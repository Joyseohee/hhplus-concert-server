package kr.hhplus.be.server.domain

interface ReservationRepository {
	fun findById(id: Long): Reservation?
	fun findByUuid(uuid: String): Reservation?
	fun save(reservation: Reservation): Reservation
	fun clear()
}