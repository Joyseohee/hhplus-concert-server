package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.model.Reservation
import kr.hhplus.be.server.domain.repository.ReservationRepository
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ReservationTable : ReservationRepository {
	private val table = ConcurrentHashMap<Long, Reservation>()

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
		if (table.containsKey(reservation.seatId) && table.containsKey(reservation.concertId)) {
			throw IllegalArgumentException("이미 예약된 좌석입니다.")
		}

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