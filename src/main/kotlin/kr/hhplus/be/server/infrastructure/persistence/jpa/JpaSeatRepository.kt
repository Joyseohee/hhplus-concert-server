package kr.hhplus.be.server.infrastructure.persistence.jpa

import kr.hhplus.be.server.domain.model.Seat
import kr.hhplus.be.server.domain.repository.SeatRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Repository
@Primary
class JpaSeatRepository(
	private val repository: SpringDataSeatRepository
) : SeatRepository {
	override fun findById(id: Long): Seat? {
		return repository.findById(id).orElse(null)
	}

	override fun findAll(): List<Seat> {
		return repository.findAll()
	}

	override fun save(seat: Seat): Seat {
		return repository.save(seat)
	}

	override fun saveAll(seats: List<Seat>): List<Seat> {
		return repository.saveAll(seats)
	}

	override fun clear() {
		repository.deleteAll()
	}

}