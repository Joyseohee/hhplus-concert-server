package kr.hhplus.be.server.domain.repository

import kr.hhplus.be.server.domain.model.SeatHold

interface SeatHoldRepository {
	fun findAll(): List<SeatHold>

	fun findById(seatHoldId: Long): SeatHold?

	fun findByUuid(seatHoldUuid: String): SeatHold?

	fun findValidSeatHoldBySeatId(seatId: Long): SeatHold?

	fun findAllByConcertId(id: Long): List<SeatHold>

	fun findHoldsToExpire(): List<SeatHold>

	fun save(seatHold: SeatHold): SeatHold

	fun deleteById(seatHold: SeatHold)

	fun deleteByIds(seatHolds: List<SeatHold>)

	fun clear()
}