package kr.hhplus.be.server.domain

interface SeatRepository {
	fun findById(id: Long?): Seat?
	fun findAll(): List<Seat> = emptyList()
	fun save(seat: Seat): Seat
	fun clear()
}