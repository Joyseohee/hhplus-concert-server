package kr.hhplus.be.server.infrastructure.lock.core

import org.redisson.api.RedissonClient
import java.util.concurrent.TimeUnit

class RedisLockManager(
	private val redisson: RedissonClient
) {

	fun <T> withLock(
		key: String,
		waitTimeMs: Long,
		leaseTimeMs: Long,
		block: () -> T
	): T {
		val lock = redisson.getLock(key)
		val acquired = lock.tryLock(waitTimeMs, leaseTimeMs, TimeUnit.MILLISECONDS)
		if (!acquired) throw LockAcquireException("락 획득에 실패하였습니다.: $key")

		try {
			return block()
		} finally {
			if (lock.isHeldByCurrentThread) lock.unlock()
		}
	}
}

class LockAcquireException(message: String) : RuntimeException(message)