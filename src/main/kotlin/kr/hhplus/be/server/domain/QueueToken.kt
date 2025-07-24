package kr.hhplus.be.server.domain

import java.time.Instant
import java.util.UUID

data class QueueToken private constructor(
	val tokenId: Long?,
	val userId: Long,
	val token: String,
	val expiresAt: Instant,
	val status: Status,
	val createdAt: Instant = Instant.now()
) {
	enum class Status {
		WAITING, ACTIVE, EXPIRED
	}
	companion object {
		const val VALID_HOLD_MINUTE = 5L
		const val MAX_ACTIVE_COUNT = 50

		fun create(
			tokenId: Long? = null,
			userId: Long,
			token: String = UUID.randomUUID().toString(),
			expiresAt: Instant = Instant.now().plusSeconds(VALID_HOLD_MINUTE * 60),
			status: Status = Status.WAITING,
		): QueueToken {
			return QueueToken(tokenId = tokenId, userId = userId, token = token, expiresAt = expiresAt, status = status)
		}
	}

	fun isValid(
	): Boolean {
		require(!isExpired()) { "토큰은 ${VALID_HOLD_MINUTE}분 동안 유효합니다. 만료 시간: ${expiresAt}" }
		if (status == Status.WAITING) {
			return false
		}

		return true
	}

	fun isExpired(): Boolean {
		return Instant.now().isAfter(expiresAt)
	}

	fun isWaiting(): Boolean {
		return status == Status.WAITING
	}

	fun isActive(): Boolean {
		return status == Status.ACTIVE
	}

	fun expireIfNeeded(now: Instant = Instant.now()): QueueToken {
        return if (expiresAt != null && expiresAt.isBefore(now) && status != Status.EXPIRED) {
            this.copy(status = Status.EXPIRED)
        } else {
            this
        }
    }

	fun activate(activeCount: Int): QueueToken {
		if (activeCount < MAX_ACTIVE_COUNT) {
			return this.copy(status = Status.ACTIVE)
		}
		return this
	}
}
