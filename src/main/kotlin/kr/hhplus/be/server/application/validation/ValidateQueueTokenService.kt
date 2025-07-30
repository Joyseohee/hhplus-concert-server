package kr.hhplus.be.server.application.validation

import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import kr.hhplus.be.server.support.error.TokenNotFoundException
import org.springframework.stereotype.Service

@Service
class ValidateQueueTokenService (
	private val queueTokenRepository: QueueTokenRepository,
	private val validateUserService: ValidateUserService
) {
	// 1) 토큰 발급 전
	fun validateUserNotToken(userId: Long) {
		validateUserService.validateUser(userId)
	}
	// 2) 토큰 검증 후
	fun validateToken(token: String): Long {
		// 토큰 조회
		val token = queueTokenRepository.findValidatedByToken(token)
			?: throw TokenNotFoundException("토큰을 찾을 수 없습니다: $token")
		validateUserService.validateUser(token.userId)
		// 기본 반환
		return token.userId
	}
}