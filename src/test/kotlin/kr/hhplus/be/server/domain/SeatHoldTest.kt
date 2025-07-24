package kr.hhplus.be.server.domain

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.SeatHold.Companion.VALID_HOLD_MINUTE
import java.time.Instant

class SeatHoldTest : FreeSpec({
    "좌석 점유 유효성 검사" - {
        val seatHoldId = 1L
        val seatHoldUuid = "uuid-1234"
        val userId = 1L
        val concertId = 1L
        val seatId = 1L

        "좌석 점유 제한 시각이 유효한 경우 점유 요청이 성공한다" {
            val seatHold = SeatHold.create(
                seatHoldId = seatHoldId,
                seatHoldUuid = seatHoldUuid,
                userId = userId,
                concertId = concertId,
                seatId = seatId,
                expiresAt = Instant.now().plusSeconds(150)
            )

            val isValid = seatHold.isValid(userId)

            isValid shouldBe true
        }

        "좌석 점유 제한 시각이 유효하지 않은 경우 예외가 발생한다" {
            val expiresAt = Instant.now().minusSeconds(150) // 2분 30초 전 만료
            val seatHold = SeatHold.create(
                seatHoldId = seatHoldId,
                seatHoldUuid = seatHoldUuid,
                userId = userId,
                concertId = concertId,
                seatId = seatId,
                expiresAt = expiresAt
            )

            val exception = shouldThrowExactly<IllegalArgumentException> {
                seatHold.isValid(userId)
            }
            exception.message shouldBe "좌석 점유는 ${VALID_HOLD_MINUTE}분 동안 유효합니다. 만료 시간: ${expiresAt}"
        }

        "좌석이 이미 예약 완료된 경우 예외가 발생한다" {
            val expiresAt = Instant.now().minusSeconds(150) // 2분 30초 전 만료
            val seatHold = SeatHold.create(
                seatHoldId = seatHoldId,
                seatHoldUuid = seatHoldUuid,
                userId = userId,
                concertId = concertId,
                seatId = seatId,
                expiresAt = expiresAt
            )

            val exception = shouldThrowExactly<IllegalArgumentException> {
                seatHold.isValid(userId)
            }
            exception.message shouldBe "좌석 점유는 ${VALID_HOLD_MINUTE}분 동안 유효합니다. 만료 시간: ${expiresAt}"
        }
    }
})


