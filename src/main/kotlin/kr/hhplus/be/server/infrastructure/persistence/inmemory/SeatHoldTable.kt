package kr.hhplus.be.server.infrastructure.persistence.inmemory

import kr.hhplus.be.server.domain.model.SeatHold
import kr.hhplus.be.server.domain.repository.SeatHoldRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Repository
class SeatHoldTable : SeatHoldRepository {
    private val table = ConcurrentHashMap<Long, SeatHold>()

    override fun findByUuid(uuid: String): SeatHold? {
        Thread.sleep(Math.random().toLong() * 200L)
        return table.values.find { it.seatHoldUuid == uuid }
    }

    override fun findByUserIdAndUuid(
        userId: Long,
        seatHoldUuid: String
    ): SeatHold? {
        Thread.sleep(Math.random().toLong() * 200L)
        return table.values.find {
            it.seatHoldUuid == seatHoldUuid && it.userId == userId && it.expiresAt.isAfter(Instant.now())
        }
    }

    override fun findValidSeatHoldBySeatId(userId: Long, seatId: Long): SeatHold? {
        Thread.sleep(Math.random().toLong() * 200L)
        return table.values.find {
                it.seatId == seatId && it.expiresAt.isAfter(Instant.now())
                && it.userId == userId
            }

    }

    override fun findValidSeatHold(
        concertId: Long,
        seatId: Long
    ): SeatHold? {
        Thread.sleep(Math.random().toLong() * 200L)
        return table.values.find {
            it.concertId == concertId && it.seatId == seatId && it.expiresAt.isAfter(Instant.now())
        }
    }

    override fun findAllConcertIdAndSeatIdAndNotExpired(
        concertId: Long,
        seatId: List<Long>
    ): List<SeatHold> {
        return table.values.filter {
            it.concertId == concertId && seatId.contains(it.seatId)
        }.sortedBy { it.expiresAt }
    }


    override fun findHoldsToExpire(): List<SeatHold> {
        return table.values.filter { it.expiresAt.isBefore(Instant.now()) }.sortedBy { it.expiresAt }
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

    override fun saveAll(seatHolds: List<SeatHold>): List<SeatHold> {
        return seatHolds.map { save(it) }
    }

    override fun deleteById(seatHold: SeatHold) {
        Thread.sleep(Math.random().toLong() * 200L)
        table.remove(seatHold.seatHoldId)
    }

    override fun deleteByIds(seatHoldIds: List<Long>) {
        Thread.sleep(Math.random().toLong() * 200L)
        table.keys.removeAll(seatHoldIds)
    }

    override fun clear() {
        table.clear()
    }
}