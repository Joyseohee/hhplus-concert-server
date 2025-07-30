package kr.hhplus.be.server.domain.repository

import kr.hhplus.be.server.domain.model.Reservation

interface ReservationRepository {
	fun findById(id: Long): Reservation?
	fun findByUuid(uuid: String): Reservation?
	fun save(reservation: Reservation): Reservation
	fun clear()
}