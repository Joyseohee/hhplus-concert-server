package kr.hhplus.be.server.infrastructure.persistence.jpa

import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Repository
@Primary
class JpaUserBalanceRepository(
	private val repository: SpringDataUserBalanceRepository
) : UserBalanceRepository {
	override fun findById(id: Long): UserBalance? {
		return repository.findById(id).orElse(null)
	}

	override fun save(userBalance: UserBalance): UserBalance {
		return repository.save(userBalance)
	}

	override fun saveAll(userBalances: List<UserBalance>): List<UserBalance> {
		return repository.saveAll(userBalances)
	}

	override fun clear() {
		repository.deleteAll()
	}
}