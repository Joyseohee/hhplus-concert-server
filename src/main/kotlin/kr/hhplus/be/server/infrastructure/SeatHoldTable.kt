package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.SeatHold
import kr.hhplus.be.server.domain.SeatHoldRepository
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class SeatHoldTable : SeatHoldRepository {
	private val table = ConcurrentHashMap<Long, SeatHold>()

	override fun findAll(): List<SeatHold> {
		Thread.sleep(Math.random().toLong() * 200L)
		return table.values.toList()
	}

	override fun findById(id: Long): SeatHold? {
		Thread.sleep(Math.random().toLong() * 200L)
		return table[id]
	}

	override fun findByUuid(uuid: String): SeatHold? {
		Thread.sleep(Math.random().toLong() * 200L)
		return table.values.find { it.seatHoldUuid == uuid }
	}

	override fun findBySeatId(seatId: Long): SeatHold? {
		Thread.sleep(Math.random().toLong() * 200L)
		return table.values.find { it.seatId == seatId }
	}

	override fun findAllByConcertId(id: Long): List<SeatHold> {
		Thread.sleep(Math.random().toLong() * 200L)
		return table.values.filter { it.concertId == id }
	}

	override fun save(
		seatHold: SeatHold
	): SeatHold {
		Thread.sleep(Math.random().toLong() * 300L)
		val seatHoldId = seatHold.seatHoldId ?: table.keys.maxOrNull()?.plus(1) ?: 1L
		val seatHold = SeatHold.create(
			seatHoldId = seatHoldId,
			seatHoldUuid = seatHold.seatHoldUuid,
			userId = seatHold.userId,
			concertId = seatHold.concertId,
			seatId = seatHold.seatId,
			expiresAt = seatHold.expiresAt,
			status = seatHold.status
		)
		table[seatHoldId] = seatHold
		return seatHold
	}

	override fun clear() {
		table.clear()
	}
}