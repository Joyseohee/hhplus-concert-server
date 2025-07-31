package kr.hhplus.be.server.application.validation

import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.stereotype.Service

@Service
class ValidateUserService(
	private val userBalanceRepository: UserBalanceRepository
) {
	fun validateUser(userId: Long) {
		val user = userBalanceRepository.findById(userId)
			?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $userId")
	}
}