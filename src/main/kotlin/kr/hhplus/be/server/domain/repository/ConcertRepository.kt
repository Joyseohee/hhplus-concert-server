package kr.hhplus.be.server.domain.repository

import kr.hhplus.be.server.domain.model.Concert

interface ConcertRepository {
	fun findById(id: Long): Concert?

	fun findAllOrderByShowDateTime(): List<Concert> = emptyList()

	fun findByIds(ids: List<Long>): List<Concert> = emptyList()

	fun save(concert: Concert): Concert

	fun saveAll(concerts: List<Concert>): List<Concert> = emptyList()

	fun clear()
}