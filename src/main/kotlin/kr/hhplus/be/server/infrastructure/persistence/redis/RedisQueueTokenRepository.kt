package kr.hhplus.be.server.infrastructure.persistence.redis

import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.model.QueueToken.Companion.HOLD_TTL
import kr.hhplus.be.server.domain.model.QueueToken.Companion.WAIT_TTL
import kr.hhplus.be.server.domain.model.QueueToken.Status
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Instant
import kotlin.text.toDouble

@Repository
@Primary
class RedisQueueTokenRepository(
	private val redisTemplate: StringRedisTemplate
) : QueueTokenRepository {
	override fun findByUserId(userId: Long): QueueToken? {
		val activatedToken =
			redisTemplate.opsForZSet().score("queue:${Status.ACTIVE.name.lowercase()}", userId.toString())
				?.let { createdAt ->
					val rank =
						redisTemplate.opsForZSet().rank("queue:${Status.ACTIVE.name.lowercase()}", userId.toString())
					QueueToken.create(
						userId = userId,
						expiresAt = Instant.ofEpochMilli(createdAt.toLong() + HOLD_TTL * 60),
						position = ((rank ?: -1) + 1).toInt(),
						status = Status.ACTIVE,
					)
				}
		if (activatedToken != null) return activatedToken

		return redisTemplate.opsForZSet().score("queue:${Status.WAITING.name.lowercase()}:${userId}", userId.toString())
			?.let { createdAt ->
				val rank =
					redisTemplate.opsForZSet().rank("queue:${Status.WAITING.name.lowercase()}", userId.toString())
				QueueToken.create(
					userId = userId,
					expiresAt = Instant.ofEpochMilli(createdAt.toLong() + WAIT_TTL * 60),
					position = ((rank ?: -1) + 1).toInt(),
					status = Status.WAITING,
				)
			}
	}

	override fun findByToken(token: String): QueueToken? {
		val userId = extractUserIdFromKey(token) ?: return null
		val waitingScore = redisTemplate.opsForZSet().score("queue:${Status.WAITING.name.lowercase()}", userId.toString())
		val activeScore = redisTemplate.opsForZSet().score("queue:${Status.ACTIVE.name.lowercase()}", userId.toString())

		return when {
			activeScore != null -> {
				val rank = redisTemplate.opsForZSet().rank("queue:${Status.ACTIVE.name.lowercase()}", userId.toString())
				QueueToken.create(
					userId = userId,
					expiresAt = Instant.ofEpochMilli(activeScore.toLong() + HOLD_TTL * 60),
					position = ((rank ?: -1) + 1).toInt(),
					status = Status.ACTIVE
				)
			}
			waitingScore != null -> {
				val rank = redisTemplate.opsForZSet().rank("queue:${Status.WAITING.name.lowercase()}", userId.toString())
				QueueToken.create(
					userId = userId,
					expiresAt = Instant.ofEpochMilli(waitingScore.toLong() + WAIT_TTL * 60),
					position = ((rank ?: -1) + 1).toInt(),
					status = Status.WAITING
				)
			}
			else -> null
		}
	}

	override fun findActiveByToken(token: String): QueueToken? {
		val userId = extractUserIdFromKey(token) ?: return null

		val key = "queue:${Status.ACTIVE.name.lowercase()}"
		redisTemplate.opsForZSet().score(key, userId.toString())
			?.let { createdAt ->
				if ((createdAt.toLong() + HOLD_TTL * 1000 * 60) > System.currentTimeMillis()) {
					val rank = redisTemplate.opsForZSet().rank(key, userId.toString())
					return QueueToken.create(
						userId = userId,
						expiresAt = Instant.ofEpochMilli(createdAt.toLong() + HOLD_TTL * 60),
						position = ((rank ?: -1) + 1).toInt(),
						status = Status.ACTIVE
					)
				}
			}

		return null
	}

	override fun findAll(): List<QueueToken> {
		return redisTemplate.keys("queue:*")
			.flatMap { key ->
				redisTemplate.opsForZSet().range(key, 0, -1)
					?.mapNotNull { userId ->
						val status = key.split(":").getOrNull(1) ?: return@mapNotNull null
						val rank = redisTemplate.opsForZSet().rank(key, userId.toString())
						QueueToken.create(
							userId = userId.toLong(),
							position = ((rank ?: -1) + 1).toInt(),
							status = Status.valueOf(status.uppercase())
						)
					} ?: emptyList()
			}
	}

	override fun countByStatus(status: QueueToken.Status): Int {
		return redisTemplate.opsForZSet().size("queue:${status.name.lowercase()}")?.toInt() ?: 0
	}

	override fun activate(userId: Long, now: Instant) {
		val waitingKey = "queue:${Status.WAITING.name.lowercase()}"
		val activeKey = "queue:${Status.ACTIVE.name.lowercase()}"

		val score = redisTemplate.opsForZSet().score(waitingKey, userId.toString())

		if (score != null) {
			redisTemplate.executePipelined { ops ->
				ops.zSetCommands().zRem(waitingKey.toByteArray(), userId.toString().toByteArray())
				ops.zSetCommands().zAdd(activeKey.toByteArray(), now.toEpochMilli().toDouble(), userId.toString().toByteArray())
				null
			}
		}
	}

	override fun save(queueToken: QueueToken): QueueToken {
		val statusKey = queueToken.status.name.lowercase()
		val ttl = when (statusKey) {
			Status.WAITING.name.lowercase() -> WAIT_TTL
			Status.ACTIVE.name.lowercase() -> HOLD_TTL
			else -> throw IllegalArgumentException("Unsupported status: ${queueToken.status}")
		}

		return redisTemplate.opsForZSet().add(
			"queue:${statusKey}",
			queueToken.userId.toString(),
			queueToken.expiresAt?.toEpochMilli()?.toDouble() ?: queueToken.createdAt.toEpochMilli().toDouble()
		).let {
			QueueToken.create(
				userId = queueToken.userId,
				status = queueToken.status,
				expiresAt = queueToken.expiresAt ?: Instant.now().plusSeconds(ttl * 60)
			)
		}
	}

	override fun deleteExpired(now: Instant) {
		val baseWait = now.toEpochMilli() - WAIT_TTL * 1000 * 60
		val baseActive = now.toEpochMilli() - HOLD_TTL * 1000 * 60

		redisTemplate.opsForZSet().removeRangeByScore(
			"queue:${Status.WAITING.name.lowercase()}",
			Double.NEGATIVE_INFINITY,
			baseWait.toDouble()
		)
		redisTemplate.opsForZSet().removeRangeByScore(
			"queue:${Status.ACTIVE.name.lowercase()}",
			Double.NEGATIVE_INFINITY,
			baseActive.toDouble()
		)
	}

	override fun deleteById(id: Long) {
		val waitingKey = "queue:${Status.WAITING.name.lowercase()}"
		val activeKey = "queue:${Status.ACTIVE.name.lowercase()}"
		redisTemplate.opsForZSet().remove(waitingKey, id.toString())
		redisTemplate.opsForZSet().remove(activeKey, id.toString())
	}

	// TEST ONLY
	override fun clear() {
		val keys = redisTemplate.keys("queue:*")
		if (keys.isNotEmpty()) {
			redisTemplate.delete(keys)
		}
	}

	fun extractStatusFromKey(key: String): String? {
		val parts = key.split(":")
		return if (parts.size == 3) parts[1] else null
	}

	fun extractUserIdFromKey(key: String): Long? {
		val parts = key.split(":")
		return if (parts.size == 3) parts[2].toLongOrNull() else null
	}
}