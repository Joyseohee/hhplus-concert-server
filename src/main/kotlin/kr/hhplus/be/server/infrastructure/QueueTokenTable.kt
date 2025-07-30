package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class QueueTokenTable : QueueTokenRepository {
	private val table = ConcurrentHashMap<Long, QueueToken>()

	override fun findAll(): List<QueueToken> {
		Thread.sleep(Math.random().toLong() * 200L)
		return table.values.toList()
	}

	override fun findById(id: Long): QueueToken? {
		Thread.sleep(Math.random().toLong() * 200L)
		return table[id]
	}

	override fun findValidatedByToken(token: String): QueueToken? {
		Thread.sleep(Math.random().toLong() * 200L)
		return table.values
				.find { it.token == token && it.status != QueueToken.Status.EXPIRED && it.expiresAt.isAfter(Instant.now()) }
	}

	override fun findWaitingTokensOrderByCreatedAt(): List<QueueToken> {
		Thread.sleep(Math.random().toLong() * 200L)
		return table.values
			.filter { it.status == QueueToken.Status.WAITING }
			.sortedBy { it.createdAt }
			.toList()
	}

	override fun findPositionByToken(token: String): Int {
		Thread.sleep(Math.random().toLong() * 200L)
		return table.values
			.filter { it.status == QueueToken.Status.WAITING }
			.sortedBy { it.createdAt }
			.indexOfFirst { it.token == token }
			.let { if (it >= 0) it + 1 else 0 }
	}

	override fun findTokenWithPosition(token: String): Pair<QueueToken, Int>? {
		Thread.sleep(Math.random().toLong() * 200L)
		return table.values
			.filter { it.status == QueueToken.Status.WAITING }
			.sortedBy { it.createdAt }
			.indexOfFirst { it.token == token }
			.takeIf { it >= 0 }
			?.let { index ->
				val queueToken = table.values
					.filter { it.status == QueueToken.Status.WAITING }
					.sortedBy { it.createdAt }[index]
				queueToken to (index + 1)
			}
	}

	override fun countByStatus(active: QueueToken.Status): Int {
		Thread.sleep(Math.random().toLong() * 200L)
		return table.values.count { it.status == active }
	}

    override fun findTokensToExpire(): List<QueueToken> {
        return table.values
            .filter { it.expiresAt.isBefore(Instant.now()) && it.status != QueueToken.Status.EXPIRED }
            .sortedBy { it.expiresAt }
            .toList()
    }

    override fun save(
        queueToken: QueueToken
    ): QueueToken {
        Thread.sleep(Math.random().toLong() * 300L)
        val tokenId = queueToken.tokenId ?: table.keys.maxOrNull()?.plus(1) ?: 1L
        val queueToken = QueueToken.create(
            tokenId = tokenId,
            userId = queueToken.userId,
            token = queueToken.token,
            status = queueToken.status,
            expiresAt = queueToken.expiresAt
        )

		table[tokenId] = queueToken
		return queueToken
	}

	override fun clear() {
		table.clear()
	}
}