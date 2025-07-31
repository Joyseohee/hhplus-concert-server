package kr.hhplus.be.server.domain.repository

import kr.hhplus.be.server.domain.model.SeatHold

interface SeatHoldRepository {

	fun findByUuid(seatHoldUuid: String): SeatHold?

	fun findValidSeatHoldBySeatId(userId: Long, seatId: Long): SeatHold?

	fun findAllConcertIdAndSeatIdAndNotExpired(concertId: Long, seatId: List<Long>): List<SeatHold>

	fun findHoldsToExpire(): List<SeatHold>

	fun save(seatHold: SeatHold): SeatHold

	fun saveAll(seatHolds: List<SeatHold>): List<SeatHold>

	fun deleteById(seatHold: SeatHold)

	fun deleteByIds(seatHoldIds: List<Long>)

	fun clear()
}