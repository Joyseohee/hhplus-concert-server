package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.model.SeatHold
import kr.hhplus.be.server.domain.repository.SeatHoldRepository
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

    override fun findValidSeatHoldBySeatId(seatId: Long): SeatHold? {
        Thread.sleep(Math.random().toLong() * 200L)
        return table.values.find {
                it.seatId == seatId && it.expiresAt.isAfter(java.time.Instant.now())
            }

    }

    override fun findAllByConcertId(id: Long): List<SeatHold> {
        Thread.sleep(Math.random().toLong() * 200L)
        return table.values.filter { it.concertId == id }
    }

    override fun findHoldsToExpire(): List<SeatHold> {
        return table.values.filter { it.expiresAt.isBefore(java.time.Instant.now()) }.sortedBy { it.expiresAt }
    }

    override fun save(
        seatHold: SeatHold
    ): SeatHold {
        Thread.sleep(Math.random().toLong() * 300L)

        val exists = table.values.any {
            it.concertId == seatHold.concertId && it.seatId == seatHold.seatId
        }
        if (exists) {
            throw IllegalArgumentException("이미 점유된 좌석입니다.")
        }

        val seatHoldId = seatHold.seatHoldId ?: table.keys.maxOrNull()?.plus(1) ?: 1L

        val seatHold = SeatHold.create(
            seatHoldId = seatHoldId,
            seatHoldUuid = seatHold.seatHoldUuid,
            userId = seatHold.userId,
            concertId = seatHold.concertId,
            seatId = seatHold.seatId,
            expiresAt = seatHold.expiresAt,
        )
        table[seatHoldId] = seatHold
        return seatHold
    }

    override fun deleteById(seatHold: SeatHold) {
        Thread.sleep(Math.random().toLong() * 200L)
        table.remove(seatHold.seatHoldId)
    }

    override fun deleteByIds(seatHolds: List<SeatHold>) {
        Thread.sleep(Math.random().toLong() * 200L)
        table.values.removeAll { seatHolds.any { hold -> hold.seatHoldId == it.seatHoldId } }
    }

    override fun clear() {
        table.clear()
    }
}