package kr.hhplus.be.server.domain

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant


class ConcertTest : FreeSpec({
    val concertId = 1L
    val title = "콘서트 제목"
    val venue = "콘서트 장소"

    "콘서트 조회" - {

        "현재 시각 이후의 콘서트는 조회가 가능하다" {
            val validShowDateTime = Instant.now().plusSeconds(3600) // 1시간 후

            val concert = Concert.create(
                concertId = concertId,
                title = title,
                venue = venue,
                showDateTime = validShowDateTime
            )

            concert.isAvailable() shouldBe true
        }

        "현재 시각 이전의 콘서트를 조회하면 예외가 발생한다" {
            val invalidShowDateTime = Instant.now().minusSeconds(3600) // 1시간 후

            val concert = Concert.create(
                concertId = concertId,
                title = title,
                venue = venue,
                showDateTime = invalidShowDateTime
            )

            listOf(0L, -10L).forEach { invalidAmount ->
                val exception = shouldThrowExactly<IllegalArgumentException> {
                    concert.isAvailable()
                }
                exception.message shouldBe  "공연이 종료되었습니다."
            }
        }
    }
})


