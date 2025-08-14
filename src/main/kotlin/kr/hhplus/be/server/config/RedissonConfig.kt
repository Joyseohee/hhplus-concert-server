package kr.hhplus.be.server.config

import kr.hhplus.be.server.infrastructure.lock.core.RedisLockManager
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class RedissonConfig(
	@Value("\${spring.data.redis.host}") private val redisHost: String,
	@Value("\${spring.data.redis.port}") private val redisPort: Int
) {

	private val REDISSON_HOST_PREFIX: String = "redis://"

	@Bean
	fun redissonClient(): RedissonClient {
		val cfg = Config()
		cfg.useSingleServer().address = "${REDISSON_HOST_PREFIX}${redisHost}:${redisPort}"
		return Redisson.create(cfg)
	}


	@Bean
	fun redisLockManager(redisson: RedissonClient): RedisLockManager =
		RedisLockManager(redisson)
}
