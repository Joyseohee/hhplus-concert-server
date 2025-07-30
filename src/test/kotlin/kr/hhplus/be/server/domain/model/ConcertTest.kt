package kr.hhplus.be.server.domain.model

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant


class ConcertTest : FreeSpec({
    val concertId = 1L
    val title = "콘서트 제목"
    val venue = "콘서트 장소"

    "콘서트 조회" - {
        "현재 시각 이후의 콘서트는 true를 반환한다" {
            val validShowDatetime = Instant.now().plusSeconds(3600) // 1시간 후
            val concert = Concert.create(
                concertId = concertId,
                title = title,
                venue = venue,
                showDateTime = validShowDatetime
            )
            concert.isAvailable() shouldBe true
        }

        "현재 시각 이전의 콘서트는 false를 반환한다" {
            listOf(Instant.now().minusSeconds(1), Instant.now().minusSeconds(3600)).forEach { invalidShowDateTime ->
                val concert = Concert.create(
                    concertId = concertId,
                    title = title,
                    venue = venue,
                    showDateTime = invalidShowDateTime
                )
                concert.isAvailable() shouldBe false
            }
        }
    }
})


