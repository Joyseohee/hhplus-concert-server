package kr.hhplus.be.server.domain

interface ConcertRepository {
	fun findById(id: Long): Concert?
	fun findAll(): List<Concert> = emptyList()
	fun save(concert: Concert): Concert
	fun clear()
}