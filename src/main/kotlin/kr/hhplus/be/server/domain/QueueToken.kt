package kr.hhplus.be.server.domain

import java.time.Instant

data class QueueToken private constructor(
	val tokenId: Long,
	val userId: Long,
	val token: String,
	val status: String,
	val expiresAt: Instant
) {
	companion object {
		fun create(
			tokenId: Long,
			userId: Long,
			token: String,
			status: String = "WAITING",
			expiresAt: Instant
		): QueueToken {
			return QueueToken(tokenId, userId, token, status, expiresAt)
		}
	}
}
