package kr.hhplus.be.server.infrastructure.persistence.inmemory

import kr.hhplus.be.server.domain.model.Concert
import kr.hhplus.be.server.domain.repository.ConcertRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Repository
class ConcertTable : ConcertRepository {
    private val table = ConcurrentHashMap<Long, Concert>()

    init {
        // 초기 데이터 설정
        table[1L] = Concert.create(
            concertId = 1L,
            title = "The Greatest Show",
            venue = "Stadium A",
            showDateTime = Instant.now().minusSeconds(3600 * 24 * 30)
        )
        table[2L] = Concert.create(
            concertId = 2L,
            title = "The Fabulous Show",
            venue = "Stadium B",
            showDateTime = Instant.now()
        )
        table[3L] = Concert.create(
            concertId = 3L,
            title = "The Best Show",
            venue = "Stadium C",
            showDateTime = Instant.now().plusSeconds(3600 * 24 * 30) // 30일 후
        )
    }

    override fun findById(id: Long): Concert? {
        Thread.sleep(Math.random().toLong() * 200L)
        return table[id]
    }

    override fun findAllOrderByShowDateTime(): List<Concert> {
        Thread.sleep(Math.random().toLong() * 200L)
        return table.values.sortedBy { it.showDateTime }
    }

    override fun save(
        concert: Concert
    ): Concert {
        Thread.sleep(Math.random().toLong() * 300L)
        val concertId = concert.concertId ?: table.keys.maxOrNull()?.plus(1) ?: 1L
        val concert = Concert.create(
            concertId = concertId,
            title = concert.title,
            venue = concert.venue,
            showDateTime = concert.showDateTime
        )

        table[concertId] = concert
        return concert
    }

    override fun saveAll(concerts: List<Concert>): List<Concert> {
        Thread.sleep(Math.random().toLong() * 300L)
        return concerts.map { save(it) }
    }

    override fun clear() {
        table.clear()
    }
}