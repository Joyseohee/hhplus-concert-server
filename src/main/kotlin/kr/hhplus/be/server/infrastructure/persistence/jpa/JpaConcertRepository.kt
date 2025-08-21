package kr.hhplus.be.server.infrastructure.persistence.jpa

import kr.hhplus.be.server.domain.model.Concert
import kr.hhplus.be.server.domain.repository.ConcertRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Repository
@Primary
class JpaConcertRepository(
	private val repository: SpringDataConcertRepository
) : ConcertRepository {
	override fun findById(id: Long): Concert? {
		return repository.findById(id).orElse(null)
	}

	override fun findAllOrderByShowDateTime(): List<Concert> {
		return repository.findAllByOrderByShowDateTimeDesc()
	}

	override fun findByIds(ids: List<Long>): List<Concert> {
		return repository.findAllById(ids)
	}

	override fun save(concert: Concert): Concert {
		return repository.save(concert)
	}

	override fun saveAll(concerts: List<Concert>): List<Concert> {
		return repository.saveAll(concerts)
	}

	override fun clear() {
		repository.deleteAll()
	}
}
