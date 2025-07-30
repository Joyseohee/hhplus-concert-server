package kr.hhplus.be.server.domain

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.hhplus.be.server.support.error.SeatHoldUnavailableException
import java.time.Instant

class SeatHoldTest : FreeSpec({
	"좌석 점유 유효성 검사" - {
		val seatHoldId = 1L
		val seatHoldUuid = "uuid-1234"
		val userId = 1L
		val concertId = 1L
		val seatId = 1L

		"좌석 점유가 유효한 경우 점유 여부를 확인하면 true를 반환한다" {
			val seatHold = SeatHold.create(
				seatHoldId = seatHoldId,
				seatHoldUuid = seatHoldUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				expiresAt = Instant.now().plusSeconds(150)
			)

			val isAvailable = seatHold.isAvailable(userId)

			isAvailable shouldBe true
		}

		"좌석 점유 시각이 만료된 경우 점유 여부를 확인하면 false를 반환한다" {
			val seatHold = SeatHold.create(
				seatHoldId = seatHoldId,
				seatHoldUuid = seatHoldUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				expiresAt = Instant.now().minusSeconds(150),
			)

			val isAvailable = seatHold.isAvailable(userId)

			isAvailable shouldBe false
		}

		"좌석 점유는 유효하지만 좌석 점유한 사용자가 아닌 경우 점유 여부를 확인하면 false를 반환한다" {
			val seatHold = SeatHold.create(
				seatHoldId = seatHoldId,
				seatHoldUuid = seatHoldUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				expiresAt = Instant.now().plusSeconds(150)
			)

			val isAvailable = seatHold.isAvailable(999L)

			isAvailable shouldBe false
		}

		"좌석 점유 제한 시각이 유효한 경우 점유 요청이 성공한다" {
			val seatHold = SeatHold.held(
				seatHoldId = seatHoldId,
				seatHoldUuid = seatHoldUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				expiresAt = Instant.now().plusSeconds(150)
			)

			seatHold shouldNotBe null
		}

		"좌석 점유 제한 시각이 유효하지 않은 경우 예외가 발생한다" {
			shouldThrowExactly<SeatHoldUnavailableException> {
				SeatHold.held(
					seatHoldId = seatHoldId,
					seatHoldUuid = seatHoldUuid,
					userId = userId,
					concertId = concertId,
					seatId = seatId,
					expiresAt = Instant.now().minusSeconds(150)
				)
			}
		}
	}
})


