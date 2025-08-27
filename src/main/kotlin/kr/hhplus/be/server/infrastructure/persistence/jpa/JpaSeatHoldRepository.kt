package kr.hhplus.be.server.infrastructure.persistence.jpa

import kr.hhplus.be.server.domain.model.SeatHold
import kr.hhplus.be.server.domain.repository.SeatHoldRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
@Primary
class JpaSeatHoldRepository(
	private val repository: SpringDataSeatHoldRepository
) : SeatHoldRepository {

	override fun findByUuid(seatHoldUuid: String): SeatHold? {
		return repository.findBySeatHoldUuid(seatHoldUuid)
	}

	override fun findByUserIdAndUuid(
		userId: Long,
		seatHoldUuid: String
	): SeatHold? {
		return repository.findByUserIdAndSeatHoldUuid(
			userId = userId,
			seatHoldUuid = seatHoldUuid
		)
	}

	override fun findValidSeatHoldBySeatId(userId: Long, seatId: Long): SeatHold? {
		return repository.findByUserIdAndSeatIdAndExpiresAtAfter(
			userId = userId,
			seatId = seatId,
			now = Instant.now()
		)
	}

	override fun findValidSeatHold(
		concertId: Long,
		seatId: Long
	): SeatHold? {
		return repository.findByConcertIdAndSeatIdAndExpiresAtAfter(
			concertId = concertId,
			seatId = seatId,
			now = Instant.now()
		)
	}

	override fun findAllConcertIdAndSeatIdAndNotExpired(concertId: Long, seatId: List<Long>): List<SeatHold> {
		return repository.findAllByConcertIdAndSeatIdInAndExpiresAtAfter(concertId = concertId, seatId = seatId, now = Instant.now())
	}

	override fun findHoldsToExpire(): List<SeatHold> {
		return repository.findByExpiresAtBeforeAndStatusIs(now = Instant.now(), status = SeatHold.Status.HOLD)
	}

	override fun save(seatHold: SeatHold): SeatHold {
		return repository.save(seatHold)
	}

	override fun saveAll(seatHolds: List<SeatHold>): List<SeatHold> {
		return repository.saveAll(seatHolds)
	}

	override fun deleteById(seatHold: SeatHold) {
		repository.delete(seatHold)
	}

	override fun deleteByIds(seatHoldIds: List<Long>) {
		repository.deleteAllById(seatHoldIds)
	}

	override fun clear() {
		repository.deleteAll()
	}

}