package kr.hhplus.be.server.infrastructure.persistence.jpa

import kr.hhplus.be.server.domain.model.SeatHold
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface SpringDataSeatHoldRepository : JpaRepository<SeatHold, Long> {
	fun findBySeatHoldUuid(seatHoldUuid: String): SeatHold?

	fun findByUserIdAndSeatHoldUuid(userId: Long, seatHoldUuid: String): SeatHold?

	fun findByUserIdAndSeatIdAndExpiresAtAfter(userId: Long, seatId: Long, now: Instant): SeatHold?

	fun findAllByConcertIdAndSeatIdInAndExpiresAtAfter(concertId: Long, seatId: List<Long>, now: Instant): List<SeatHold>

	fun findByExpiresAtBeforeAndStatusIs(now: Instant, status: SeatHold.Status): List<SeatHold>

	fun findByConcertIdAndSeatIdAndExpiresAtAfter(concertId: Long, seatId: Long, now: Instant): SeatHold?
}
