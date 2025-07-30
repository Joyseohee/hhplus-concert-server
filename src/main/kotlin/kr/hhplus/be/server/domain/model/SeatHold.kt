package kr.hhplus.be.server.domain.model

import kr.hhplus.be.server.support.error.SeatHoldUnavailableException
import java.time.Instant

data class SeatHold private constructor(
	val seatHoldId: Long?,
	val seatHoldUuid: String,
	val userId: Long,
	val concertId: Long,
	val seatId: Long,
	val expiresAt: Instant,
) {
	companion object {
		const val VALID_HOLD_MINUTE = 5L

		fun create(
			seatHoldId: Long? = null,
			seatHoldUuid: String,
			userId: Long,
			concertId: Long,
			seatId: Long,
			expiresAt: Instant = Instant.now().plusSeconds(VALID_HOLD_MINUTE * 60),
		): SeatHold {
			return SeatHold(
				seatHoldId = seatHoldId,
				seatHoldUuid = seatHoldUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				expiresAt = expiresAt,
			)
		}

		fun held(
			seatHoldId: Long? = null,
			seatHoldUuid: String,
			userId: Long,
			concertId: Long,
			seatId: Long,
			expiresAt: Instant = Instant.now().plusSeconds(VALID_HOLD_MINUTE * 60),
		): SeatHold {
			if (expiresAt.isBefore(Instant.now())) {
				throw SeatHoldUnavailableException("좌석 점유 제한 시각은 현재 시각 이후여야 합니다. 현재: ${Instant.now()}, 만료 시각: $expiresAt")
			}

			return create(
				seatHoldId = seatHoldId,
				seatHoldUuid = seatHoldUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				expiresAt = expiresAt,
			)
		}
	}

	fun isAvailable(userId: Long): Boolean {
		return !isExpired() && this.userId == userId
	}

	// 점유 유효성 검증
	private fun isValid(userId: Long) {
		if (isExpired()) throw SeatHoldUnavailableException("좌석 점유는 ${VALID_HOLD_MINUTE}분 동안 유효합니다. 만료 시간: ${expiresAt}")
		if (this.userId != userId) throw SeatHoldUnavailableException("점유한 사용자가 아닙니다.")
	}

	private fun isExpired(): Boolean {
		return Instant.now().isAfter(expiresAt)
	}
}
