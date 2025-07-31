package kr.hhplus.be.server.infrastructure.persistence.jpa

import kr.hhplus.be.server.domain.model.Reservation
import kr.hhplus.be.server.domain.repository.ReservationRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Repository
@Primary
class JpaReservationRepository(
	private val repository: SpringDataReservationRepository
) : ReservationRepository {
	override fun findBySeatId(seatId: Long): Reservation? {
		return repository.findBySeatId(seatId)
	}

	override fun findAllBySeatId(seatIds: List<Long>): List<Reservation> {
		return repository.findAllBySeatIdIn(seatIds)
	}

	override fun save(reservation: Reservation): Reservation {
		return repository.save(reservation)
	}

	override  fun clear() {
		repository.deleteAll()
	}

}