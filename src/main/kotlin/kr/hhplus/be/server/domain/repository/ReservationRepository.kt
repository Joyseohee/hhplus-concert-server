package kr.hhplus.be.server.domain.repository

import kr.hhplus.be.server.domain.model.Reservation

interface ReservationRepository {
	fun findByIdOrElseThrow(id: Long): Reservation?

	fun findByUuid(uuid: String): Reservation?

	fun findBySeatId(seatId: Long): Reservation?

	fun findAllBySeatId(seatIds: List<Long>): List<Reservation> = emptyList()

	fun count(): Long

	fun save(reservation: Reservation): Reservation

	fun clear()


}