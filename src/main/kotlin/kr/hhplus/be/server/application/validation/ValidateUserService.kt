package kr.hhplus.be.server.application.validation

import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import kr.hhplus.be.server.support.error.UserNotFoundException
import org.springframework.stereotype.Service

@Service
class ValidateUserService(
	private val userBalanceRepository: UserBalanceRepository
) {
	fun validateUser(userId: Long) {
		// 사용자 조회
		val user = userBalanceRepository.findById(userId)
			?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userId")
	}
}