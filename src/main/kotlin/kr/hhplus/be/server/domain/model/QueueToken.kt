package kr.hhplus.be.server.domain.model

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

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
		const val WAIT_TTL = 30L
		const val HOLD_TTL = 5L
		const val MAX_ACTIVE_COUNT = 50

		fun create(
			tokenId: Long? = null,
			userId: Long,
			token: String = UUID.randomUUID().toString(),
			expiresAt: Instant = Instant.now().plus(WAIT_TTL, ChronoUnit.MINUTES),
			status: Status = Status.WAITING,
		): QueueToken {
			return QueueToken(tokenId = tokenId, userId = userId, token = token, expiresAt = expiresAt, status = status)
		}
	}

	fun isValid(): Boolean {
		return expiresAt.isAfter(Instant.now()) && status != Status.EXPIRED
	}

	fun expire(): QueueToken {
		if (expiresAt.isBefore(Instant.now()) || status == Status.EXPIRED) {
			return this.copy(status = Status.EXPIRED)
		}
		return this
	}

	fun activate(position: Int): QueueToken {
		if (!isValid()) {
			throw IllegalArgumentException("Token is expired or invalid")
		}

		if (position <= MAX_ACTIVE_COUNT) {
			return this.copy(status = Status.ACTIVE, expiresAt = Instant.now().plus(HOLD_TTL, ChronoUnit.MINUTES))
		}
		return this
	}
}
