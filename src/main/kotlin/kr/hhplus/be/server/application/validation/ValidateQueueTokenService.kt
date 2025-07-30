package kr.hhplus.be.server.application.validation

import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import kr.hhplus.be.server.support.error.TokenNotFoundException
import org.springframework.stereotype.Service

@Service
class ValidateQueueTokenService(
	private val queueTokenRepository: QueueTokenRepository
) {
	fun validateToken(token: String): QueueToken {
		// 토큰 조회
		val original = queueTokenRepository.findValidatedByToken(token)
			?: throw TokenNotFoundException("토큰을 찾을 수 없습니다: $token")

		// 만료 체크 및 갱신
		val checked = original.expire()

		// 기본 반환
		return checked
	}
}