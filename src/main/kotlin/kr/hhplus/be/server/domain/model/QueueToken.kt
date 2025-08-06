package kr.hhplus.be.server.domain.model

import jakarta.persistence.*
import kr.hhplus.be.server.infrastructure.persistence.jpa.BaseEntity
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Entity
@Table(name = "tokens")
class QueueToken private constructor(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val tokenId: Long? = null,
	@Column(name = "user_id", nullable = false)
	val userId: Long,
	@Column(name = "token", nullable = false, unique = true)
	val token: String,
	@Column(name = "expires_at", nullable = false)
	var expiresAt: Instant,
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
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

	fun expire() {
		if (expiresAt.isBefore(Instant.now()) || status == Status.EXPIRED) {
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
		tokenId: Long? = this.tokenId,
		userId: Long = this.userId,
		token: String = this.token,
		expiresAt: Instant = this.expiresAt,
		status: Status = this.status
	): QueueToken {
		return QueueToken(
			tokenId = tokenId ?: this.tokenId,
			userId = userId,
			token = token,
			expiresAt = expiresAt,
			status = status
		)
	}
}
