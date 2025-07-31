package kr.hhplus.be.server.infrastructure.persistence.inmemory

import kr.hhplus.be.server.domain.model.Seat
import kr.hhplus.be.server.domain.repository.SeatRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class SeatTable : SeatRepository {
    private val table = ConcurrentHashMap<Long, Seat>()

    init {
        // 초기 데이터 설정 (예시)
        table[1L] = Seat.create(
            seatId = 1L,
            seatNumber = 1,
            price = 100000
        )
        table[2L] = Seat.create(
            seatId = 2L,
            seatNumber = 30,
            price = 120000
        )
    }

    override fun findById(id: Long): Seat? {
        Thread.sleep(Math.random().toLong() * 200L)
        return table[id]
    }

    override fun findAll(): List<Seat> {
        Thread.sleep(Math.random().toLong() * 200L)
        return table.values.toList()
    }

    override fun save(
        seat: Seat
    ): Seat {
        Thread.sleep(Math.random().toLong() * 300L)
        val seatId = seat.seatId ?: table.keys.maxOrNull()?.plus(1) ?: 1L
        val seat = Seat.create(
            seatId = seatId,
            seatNumber = seat.seatNumber,
            price = seat.price
        )

        table[seatId] = seat
        return seat
    }

    override fun saveAll(
        seats: List<Seat>
    ): List<Seat> {
        Thread.sleep(Math.random().toLong() * 300L)
        return seats.map { save(it) }
    }

    override fun clear() {
        table.clear()
    }
}