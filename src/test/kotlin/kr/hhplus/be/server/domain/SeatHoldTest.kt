package kr.hhplus.be.server.domain

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.SeatHold.Status
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

			seatHold.status shouldBe Status.HELD
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

		"좌석 점유가 유효한 경우 예약하면 성공한다" {
			val expiresAt = Instant.now().plusSeconds(150) // 2분 30초 전 만료
			val seatHold = SeatHold.held(
				seatHoldId = seatHoldId,
				seatHoldUuid = seatHoldUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				expiresAt = expiresAt
			)

			val reserved = seatHold.reserved(userId)
			reserved.seatHoldId shouldBe seatHoldId
			reserved.seatHoldUuid shouldBe seatHoldUuid
			reserved.status shouldBe Status.RESERVED
		}

		"좌석 점유 제한 시각이 유효하지 않은 경우 예약하면 예외가 발생한다" {
			val expiresAt = Instant.now().minusSeconds(150) // 2분 30초 전 만료
			val seatHold = SeatHold.create(
				seatHoldId = seatHoldId,
				seatHoldUuid = seatHoldUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				expiresAt = expiresAt
			)

			shouldThrowExactly<SeatHoldUnavailableException> {
				seatHold.reserved(userId)
			}
		}

		"이미 예약된 좌석을 예약하면 예외가 발생한다" {
			val expiresAt = Instant.now().plusSeconds(150) // 2분 30초 전 만료
			val seatHold = SeatHold.create(
				seatHoldId = seatHoldId,
				seatHoldUuid = seatHoldUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				expiresAt = expiresAt,
				status = Status.RESERVED
			)

			shouldThrowExactly<SeatHoldUnavailableException> {
				seatHold.reserved(userId)
			}
		}

		"이미 만료된 좌석을 예약하면 예외가 발생한다" {
			val expiresAt = Instant.now().plusSeconds(150) // 2분 30초 전 만료
			val seatHold = SeatHold.create(
				seatHoldId = seatHoldId,
				seatHoldUuid = seatHoldUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				expiresAt = expiresAt,
				status = Status.EXPIRED
			)

			shouldThrowExactly<SeatHoldUnavailableException> {
				seatHold.reserved(userId)
			}
		}

		"좌석 점유 시간이 만료되었으며 만료를 요청한 당사자인 경우 만료 처리에 성공한다" {
			val expiresAt = Instant.now().minusSeconds(150) // 2분 30초 전 만료
			val seatHold = SeatHold.create(
				seatHoldId = seatHoldId,
				seatHoldUuid = seatHoldUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				expiresAt = expiresAt
			)

			val expired = seatHold.expired(userId)
			expired.seatHoldId shouldBe seatHoldId
			expired.seatHoldUuid shouldBe seatHoldUuid
			expired.status shouldBe Status.EXPIRED
		}

		"좌석이 예약되었으며 예약한 당사자인 경우 만료 처리에 성공한다" {
			val expiresAt = Instant.now().plusSeconds(150) // 2분 30초 전 만료
			val seatHold = SeatHold.create(
				seatHoldId = seatHoldId,
				seatHoldUuid = seatHoldUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				expiresAt = expiresAt,
				status = Status.RESERVED
			)

			val expired = seatHold.expired(userId)
			expired.seatHoldId shouldBe seatHoldId
			expired.seatHoldUuid shouldBe seatHoldUuid
			expired.status shouldBe Status.EXPIRED
		}

		"당사자가 아니라도 스케줄러라면 만료 처리에 성공한다" {
			val expiresAt = Instant.now().minusSeconds(150) // 2분 30초 전 만료
			val seatHold = SeatHold.create(
				seatHoldId = seatHoldId,
				seatHoldUuid = seatHoldUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				expiresAt = expiresAt
			)

			val expired = seatHold.expired(isScheduler = true)
			expired.seatHoldId shouldBe seatHoldId
			expired.seatHoldUuid shouldBe seatHoldUuid
			expired.status shouldBe Status.EXPIRED
		}

		"당사자가 아니고 스케줄러도 아니라면 예외가 발생한다" {
			val expiresAt = Instant.now().minusSeconds(150) // 2분 30초 전 만료
			val seatHold = SeatHold.create(
				seatHoldId = seatHoldId,
				seatHoldUuid = seatHoldUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				expiresAt = expiresAt
			)

			shouldThrowExactly<IllegalArgumentException> {
				seatHold.expired(isScheduler = false, userId = 999L)
			}
		}
	}
})


