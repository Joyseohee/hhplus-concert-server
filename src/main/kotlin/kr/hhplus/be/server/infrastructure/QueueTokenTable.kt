package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.QueueToken
import kr.hhplus.be.server.domain.QueueTokenRepository
import org.springframework.stereotype.Component

@Component
class QueueTokenTable : QueueTokenRepository {
    private val table = HashMap<Long, QueueToken>()

    override fun findAll(): List<QueueToken> {
        Thread.sleep(Math.random().toLong() * 200L)
        return table.values.toList()
    }

    override fun findById(id: Long): QueueToken? {
        Thread.sleep(Math.random().toLong() * 200L)
        return table[id]
    }

    override fun findByToken(token: String): QueueToken? {
        Thread.sleep(Math.random().toLong() * 200L)
        return table.values.firstOrNull { it.token == token }
    }

    override fun findWaitingTokensOrderByCreatedAt(): List<QueueToken> {
        Thread.sleep(Math.random().toLong() * 200L)
        return table.values
            .filter { it.status == QueueToken.Status.WAITING }
            .sortedBy { it.createdAt }
            .toList()
    }

    override fun findPositionByToken(token: String): Int? {
        Thread.sleep(Math.random().toLong() * 200L)
        return table.values
            .filter { it.status == QueueToken.Status.WAITING }
            .sortedBy { it.createdAt }
            .indexOfFirst { it.token == token }
            .takeIf { it >= 0 }
            ?.plus(1)
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