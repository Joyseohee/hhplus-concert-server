package kr.hhplus.be.server.domain.repository

import kr.hhplus.be.server.domain.model.QueueToken

interface QueueTokenRepository {

	fun findByUserId(userId: Long): QueueToken?

	fun findValidatedByToken(token: String): QueueToken?

	fun findAllWaitingTokenForActivate(i: Int): List<QueueToken>

	fun findPositionById(id: Long): Int?

	fun findTokensToExpire(): List<QueueToken>

	fun countByStatus(active: QueueToken.Status): Int

	fun save(queueToken: QueueToken): QueueToken

	fun saveAll(queueTokens: List<QueueToken>): List<QueueToken>

	fun deleteById(id: Long)

	fun deleteByIds(ids: List<Long>)

	fun clear()

}