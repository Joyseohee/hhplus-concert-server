package kr.hhplus.be.server.infrastructure.persistence.redis

import kr.hhplus.be.server.domain.repository.ConcertAggregationRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository

@Repository
class RedisConcertAggregationRepository(
    private val redisTemplate: StringRedisTemplate
) : ConcertAggregationRepository {
    override fun getTopConcertIds(key: String, limit: Long): List<Long> {
        val ids = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1)
        return ids?.map { it.toLong() } ?: emptyList()
    }

    override fun incrementScore(key: String, concertId: Long) {
        redisTemplate.opsForZSet().incrementScore(key, concertId.toString(), 1.0)
    }

    override fun incrementScore(key: String, concertId: Long, score: Double) {
        redisTemplate.opsForZSet().incrementScore(key, concertId.toString(), score)
    }

    override fun clear(key: String) {
        val keys = redisTemplate.keys(key)
        if (keys.isNotEmpty()) {
            redisTemplate.delete(keys)
        }
    }

}