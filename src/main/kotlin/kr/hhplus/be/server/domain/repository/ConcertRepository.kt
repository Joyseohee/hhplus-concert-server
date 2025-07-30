package kr.hhplus.be.server.domain.repository

import kr.hhplus.be.server.domain.model.Concert

interface ConcertRepository {
	fun findById(id: Long): Concert?
	fun findAll(): List<Concert> = emptyList()
	fun save(concert: Concert): Concert
	fun clear()
}