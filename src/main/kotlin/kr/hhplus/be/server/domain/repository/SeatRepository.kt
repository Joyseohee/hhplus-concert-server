package kr.hhplus.be.server.domain.repository

import kr.hhplus.be.server.domain.model.Seat

interface SeatRepository {
	fun findById(id: Long?): Seat?
	fun findAll(): List<Seat> = emptyList()
	fun save(seat: Seat): Seat
	fun clear()
}