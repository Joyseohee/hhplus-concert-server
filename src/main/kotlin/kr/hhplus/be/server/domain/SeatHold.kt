package kr.hhplus.be.server.domain

import kr.hhplus.be.server.support.error.SeatHoldUnavailableException
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

		fun held(
			seatHoldId: Long,
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
				status = Status.HELD
			)
		}
	}

	fun reserved(userId: Long): SeatHold {
		isValid(userId)
		return this.copy(status = Status.RESERVED)
	}

	fun expired(userId: Long? = null, isScheduler: Boolean? = false): SeatHold {
		require(isReserved() || isExpired()) { "좌석 점유가 만료되지 않았습니다." }
		if (userId == null && isScheduler == false) {
			throw IllegalArgumentException("좌석을 점유한 사용자가 아닙니다.")
		}

		if (userId != null && this.userId != userId) {
			throw IllegalArgumentException("좌석을 점유한 사용자가 아닙니다.")
		}

		return this.copy(status = Status.EXPIRED)
	}

	fun isAvailable(userId: Long): Boolean {
		if (isExpired()) return false
		if (isReserved() && this.userId != userId) return false
		if (isHeld() && this.userId != userId) return false
		return true
	}

	// 점유 유효성 검증
	private fun isValid(userId: Long) {
		if (isExpired()) throw SeatHoldUnavailableException("좌석 점유는 ${VALID_HOLD_MINUTE}분 동안 유효합니다. 만료 시간: ${expiresAt}")
		if (isReserved()) throw SeatHoldUnavailableException("이미 예매된 좌석입니다.")
		if (this.userId != userId) throw SeatHoldUnavailableException("점유한 사용자가 아닙니다.")
	}

	private fun isHeld(): Boolean {
		return status == Status.HELD
	}

	private fun isReserved(): Boolean {
		return status == Status.RESERVED
	}

	private fun isExpired(): Boolean {
		return status == Status.EXPIRED || Instant.now().isAfter(expiresAt)
	}
}
