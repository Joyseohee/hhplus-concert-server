package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.Concert
import kr.hhplus.be.server.domain.ConcertRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ConcertTable(
    @Value("\${spring.profiles.active:default}") private val activeProfile: String
) : ConcertRepository {
    private val table = HashMap<Long, Concert>()

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

    override fun findAll(): List<Concert> {
        Thread.sleep(Math.random().toLong() * 200L)
        return table.values.toList()
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

    override fun clear() {
        table.clear()
    }
}