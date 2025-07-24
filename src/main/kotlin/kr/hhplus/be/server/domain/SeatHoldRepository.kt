package kr.hhplus.be.server.domain

interface SeatHoldRepository {
	fun findById(seatHoldId: Long): SeatHold?
	fun findByUuid(seatHoldUuid: String): SeatHold?
	fun findBySeatId(seatId: Long): SeatHold?
	fun findAllByConcertId(id: Long): List<SeatHold>
	fun save(seatHold: SeatHold): SeatHold
	fun clear()
}