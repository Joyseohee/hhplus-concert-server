package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.QueueToken
import kr.hhplus.be.server.domain.QueueTokenRepository
import org.springframework.stereotype.Component

@Component
class QueueTokenTable : QueueTokenRepository {
    private val table = HashMap<Long, QueueToken>()

    override fun findById(id: Long): QueueToken? {
        Thread.sleep(Math.random().toLong() * 200L)
        return table[id]
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
}