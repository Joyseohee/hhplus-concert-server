package kr.hhplus.be.server.application.validation

import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.stereotype.Service

@Service
class ValidateQueueTokenService(
	private val queueTokenRepository: QueueTokenRepository,
	private val userBalanceRepository: UserBalanceRepository
) {
	fun validateToken(token: String): Long {
		val token = queueTokenRepository.findActiveByToken(token)
			?: throw IllegalArgumentException("토큰을 찾을 수 없습니다: $token")

		val user = userBalanceRepository.findById(token.userId)
			?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $token.userId")

		return user.userId!!
	}
}