package kr.hhplus.be.server.infrastructure.persistence.jpa

import kr.hhplus.be.server.domain.model.Reservation
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataReservationRepository : JpaRepository<Reservation, Long> {
	fun findBySeatId(seatId: Long): Reservation?
	fun findAllBySeatIdIn(seatIds: List<Long>): List<Reservation>
}
