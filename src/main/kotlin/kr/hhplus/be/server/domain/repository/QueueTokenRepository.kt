package kr.hhplus.be.server.domain.repository

import kr.hhplus.be.server.domain.model.QueueToken
import java.time.Instant

interface QueueTokenRepository {

	fun findByUserId(userId: Long): QueueToken?

	fun findByToken(token: String): QueueToken?

	fun findActiveByToken(token: String): QueueToken?

	fun findAll(): List<QueueToken>

	fun countByStatus(active: QueueToken.Status): Int

	fun activate(count: Int, now: Instant)

	fun save(queueToken: QueueToken): QueueToken

	fun deleteExpired(now: Instant)

	fun deleteById(id: Long)

	fun clear()

}