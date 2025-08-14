package kr.hhplus.be.server.infrastructure.lock.aspect


import kr.hhplus.be.server.infrastructure.lock.core.LockAcquireException
import kr.hhplus.be.server.infrastructure.lock.core.RedisLockManager
import kr.hhplus.be.server.support.annotation.RedisLock
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RedisLockAspect(
	private val redisLockManager: RedisLockManager
) {
	private val keyParser = SpELLockKeyParser()

	@Around("@annotation(kr.hhplus.be.server.support.annotation.RedisLock)")
	fun around(pjp: ProceedingJoinPoint): Any? {
		val method = (pjp.signature as MethodSignature).method
		val redisLock = method.getAnnotation(RedisLock::class.java)
			?: throw IllegalArgumentException("RedisLock 어노테이션이 없습니다")

		val key = keyParser.parse(pjp, redisLock.key)
		val maxRetries = redisLock.maxRetries
		val retryDelayMs = redisLock.retryDelayMs

		var lastError: Exception? = null

		for (attempt in 0..maxRetries) {
			try {
				return redisLockManager.withLock(key, redisLock.waitTimeMs, redisLock.leaseTimeMs) {
					pjp.proceed()
				}
			} catch (e: Exception) {
				if (e is LockAcquireException && !redisLock.failFast && attempt < maxRetries) {
					lastError = e

					if (retryDelayMs > 0) Thread.sleep(retryDelayMs)
					continue
				}
				throw e
			}
		}

		throw (lastError ?: LockAcquireException("분산락 키 획득에 실패했습니다 : key=$key"))
	}

}