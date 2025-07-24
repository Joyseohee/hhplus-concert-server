package kr.hhplus.be.server.domain

import java.time.Instant

data class SeatHold private constructor(
	val seatHoldId: Long?,
	val seatHoldUuid: String,
	val userId: Long,
	val concertId: Long,
	val seatId: Long,
	val expiresAt: Instant,
	val status: Status
) {
	enum class Status {
		HELD, RESERVED, EXPIRED
	}

	companion object {
		const val VALID_HOLD_MINUTE = 5L

		fun create(
			seatHoldId: Long? = null,
			seatHoldUuid: String,
			userId: Long,
			concertId: Long,
			seatId: Long,
			expiresAt: Instant = Instant.now().plusSeconds(VALID_HOLD_MINUTE * 60),
			status: Status = Status.HELD
		): SeatHold {
			return SeatHold(
				seatHoldId = seatHoldId,
				seatHoldUuid = seatHoldUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				expiresAt = expiresAt,
				status = status
			)
		}
	}

	// 점유 유효성 검증
	fun isValid(userId: Long): Boolean {
		require(!isExpired()) { "좌석 점유는 ${VALID_HOLD_MINUTE}분 동안 유효합니다. 만료 시간: ${expiresAt}" }
		require(!isReserved()) { "이미 예매된 좌석입니다." }
		require(this.userId == userId) { "점유한 사용자가 아닙니다." }

		return true
	}

	fun isAvailable(userId: Long): Boolean {
		if (isExpired()) return false
		if (isReserved() && this.userId != userId) return false
		if (isHeld() && this.userId != userId) return false
		return true
	}

	fun isHeld(): Boolean {
		return status == Status.HELD
	}

	fun isReserved(): Boolean {
		return status == Status.RESERVED
	}

	fun isExpired(): Boolean {
		return status == Status.EXPIRED || Instant.now().isAfter(expiresAt)
	}
}
