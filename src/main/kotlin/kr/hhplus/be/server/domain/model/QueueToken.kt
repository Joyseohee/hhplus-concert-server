package kr.hhplus.be.server.domain.model

import kr.hhplus.be.server.infrastructure.persistence.jpa.BaseEntity
import java.time.Instant
import java.time.temporal.ChronoUnit

class QueueToken private constructor(
	val userId: Long,
	val token: String,
	var position: Int = 0,
	var expiresAt: Instant? = null,
	var status: Status,
) : BaseEntity() {
	enum class Status {
		WAITING, ACTIVE, EXPIRED
	}

	companion object {
		const val WAIT_TTL = 30L
		const val HOLD_TTL = 5L
		const val MAX_ACTIVE_COUNT = 50

		fun create(
			userId: Long,
			position: Int = 0,
			expiresAt: Instant? = null,
			status: Status = Status.WAITING,
		): QueueToken {
			return QueueToken(userId = userId, token = "queue:${status.name.lowercase()}:${userId}", position = position, expiresAt = expiresAt, status = status)
		}
	}

	fun isValid(): Boolean {
		return expiresAt!!.isAfter(Instant.now()) && status != Status.EXPIRED
	}

	fun expire() {
		if (expiresAt!!.isBefore(Instant.now()) || status == Status.EXPIRED) {
			status = Status.EXPIRED
		}
	}

	fun activate(position: Int) {
		if (!isValid()) {
			throw IllegalArgumentException("Token is expired or invalid")
		}

		if (position <= MAX_ACTIVE_COUNT) {
			status = Status.ACTIVE
			expiresAt = Instant.now().plus(HOLD_TTL, ChronoUnit.MINUTES)
		}
	}

	fun copy(
		userId: Long = this.userId,
		token: String = this.token,
		expiresAt: Instant? = this.expiresAt,
		position: Int = this.position,
		status: Status = this.status
	): QueueToken {
		return QueueToken(
			userId = userId,
			token = token,
			expiresAt = expiresAt,
			position = position,
			status = status
		)
	}
}
