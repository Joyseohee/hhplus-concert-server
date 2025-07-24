package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.Reservation
import kr.hhplus.be.server.domain.ReservationRepository
import org.springframework.stereotype.Component

@Component
class ReservationTable : ReservationRepository {
	private val table = HashMap<Long, Reservation>()

	override fun findById(id: Long): Reservation? {
		Thread.sleep(Math.random().toLong() * 200L)
		return table[id]
	}

	override fun findByUuid(uuid: String): Reservation? {
		Thread.sleep(Math.random().toLong() * 200L)
		return table.values.firstOrNull { it.reservationUuid == uuid }
	}

	override fun save(
		reservation: Reservation
	): Reservation {
		Thread.sleep(Math.random().toLong() * 300L)
		val reservationId = reservation.reservationId ?: table.keys.maxOrNull()?.plus(1) ?: 1L
		val reservation = Reservation.create(
			reservationId = reservationId,
			reservationUuid = reservation.reservationUuid,
			userId = reservation.userId,
			concertId = reservation.concertId,
			seatId = reservation.seatId,
			reservedAt = reservation.reservedAt,
			price = reservation.price
		)

		table[reservationId] = reservation
		return reservation
	}

	override fun clear() {
		table.clear()
	}
}