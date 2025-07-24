package kr.hhplus.be.server.domain

interface QueueTokenRepository {
	fun findById(id: Long): QueueToken?
	fun save(queueToken: QueueToken): QueueToken
}