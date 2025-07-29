package kr.hhplus.be.server.domain

interface SeatHoldRepository {
	fun findAll(): List<SeatHold>

	fun findById(seatHoldId: Long): SeatHold?

	fun findByUuid(seatHoldUuid: String): SeatHold?

	fun findBySeatId(seatId: Long): SeatHold?

	fun findAllByConcertId(id: Long): List<SeatHold>

	fun findHoldsToExpire(): List<SeatHold>

	fun save(seatHold: SeatHold): SeatHold

	fun clear()
}