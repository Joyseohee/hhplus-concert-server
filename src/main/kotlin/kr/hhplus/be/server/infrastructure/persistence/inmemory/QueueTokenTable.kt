package kr.hhplus.be.server.infrastructure.persistence.inmemory

import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Repository
class QueueTokenTable : QueueTokenRepository  {
	private val table = ConcurrentHashMap<Long, QueueToken>()

	override fun findByUserId(userId: Long): QueueToken? {
		Thread.sleep(Math.random().toLong() * 200L)
		return table.values
				.find { it.userId == userId && it.status != QueueToken.Status.EXPIRED && it.expiresAt.isAfter(Instant.now()) }
	}

	override fun findValidatedByToken(token: String): QueueToken? {
		Thread.sleep(Math.random().toLong() * 200L)
		return table.values
				.find { it.token == token && it.status != QueueToken.Status.EXPIRED && it.expiresAt.isAfter(Instant.now()) }
	}

	override fun findAllWaitingTokenForActivate(i: Int): List<QueueToken> {
		return table.values
			.filter { it.status == QueueToken.Status.WAITING }
			.sortedBy { it.createdAt }
			.take(i)
			.map { it.copy(status = QueueToken.Status.ACTIVE) }
			.toList()
	}

	override fun findPositionById(id: Long): Int {
		Thread.sleep(Math.random().toLong() * 200L)
		return table.values
			.filter { it.status == QueueToken.Status.WAITING }
			.sortedBy { it.createdAt }
			.indexOfFirst { it.tokenId == id }
			.let { if (it >= 0) it + 1 else 0 }
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

	override fun saveAll(queueTokens: List<QueueToken>): List<QueueToken> {
		return queueTokens.map { save(it) }
	}

	override fun deleteById(id: Long) {
		Thread.sleep(Math.random().toLong() * 200L)
		table.remove(id)
	}

	override fun deleteByIds(ids: List<Long>) {
		Thread.sleep(Math.random().toLong() * 200L)
		ids.forEach { table.remove(it) }
	}

	override fun clear() {
		table.clear()
	}
}