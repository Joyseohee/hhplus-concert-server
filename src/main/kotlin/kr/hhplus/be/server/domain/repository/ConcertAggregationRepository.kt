package kr.hhplus.be.server.domain.repository

interface ConcertAggregationRepository {
	fun getTopConcertIds(key: String, limit: Long): List<Long>
	fun incrementScore(key: String, concertId: Long)
	fun incrementScore(key: String, concertId: Long, score: Double)
	fun clear(key: String)
}