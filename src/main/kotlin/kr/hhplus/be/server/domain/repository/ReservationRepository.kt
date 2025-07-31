package kr.hhplus.be.server.domain.repository

import kr.hhplus.be.server.domain.model.Reservation

interface ReservationRepository {
	fun findBySeatId(seatId: Long): Reservation?

	fun findAllBySeatId(seatIds: List<Long>): List<Reservation> = emptyList()

	fun save(reservation: Reservation): Reservation

	fun clear()


}