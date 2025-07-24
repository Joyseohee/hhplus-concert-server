package kr.hhplus.be.server.domain

interface QueueTokenRepository {
	fun findAll(): List<QueueToken>

	fun findById(id: Long): QueueToken?

	fun findByToken(token: String): QueueToken?

	fun findWaitingTokensOrderByCreatedAt(): List<QueueToken>

	fun findPositionByToken(token: String): Int?

	fun findTokenWithPosition(token: String): Pair<QueueToken, Int>?

	fun countByStatus(active: QueueToken.Status): Int

	fun save(queueToken: QueueToken): QueueToken

	fun clear()

}