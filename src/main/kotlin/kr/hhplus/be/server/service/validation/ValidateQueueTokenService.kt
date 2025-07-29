package kr.hhplus.be.server.service.validation

import kr.hhplus.be.server.domain.QueueToken
import kr.hhplus.be.server.domain.QueueTokenRepository
import kr.hhplus.be.server.domain.UserBalanceRepository
import kr.hhplus.be.server.support.error.TokenNotAvailable
import kr.hhplus.be.server.support.error.TokenNotFoundException
import kr.hhplus.be.server.support.error.UserNotFoundException
import org.springframework.stereotype.Service

// todo - getTokenService와 통합
@Service
class ValidateQueueTokenService(
	private val userBalanceRepository: UserBalanceRepository,
	private val queueTokenRepository: QueueTokenRepository
) {
	fun validateToken(token: String): QueueToken {
		// 토큰 조회
		val original = queueTokenRepository.findByToken(token)
			?: throw TokenNotFoundException("토큰을 찾을 수 없습니다.")

		// 만료 체크 및 갱신
		val checked = original.expire()
		if (checked.status == QueueToken.Status.EXPIRED && original.status != QueueToken.Status.EXPIRED) {
			queueTokenRepository.save(checked)
		}

		// 만료된 토큰인지 확인
		if (checked.status == QueueToken.Status.EXPIRED) {
			throw TokenNotAvailable("토큰은 만료되었습니다.")
		}

		// 사용자 존재 확인
		userBalanceRepository.findById(checked.userId)
			?: throw UserNotFoundException("사용자를 찾을 수 없습니다.")

		// Active 토큰 수 조회
		val activeCount = queueTokenRepository.countByStatus(QueueToken.Status.ACTIVE)

		// 대기 중인 토큰이라면
		if (checked.status == QueueToken.Status.WAITING) {
			val activatedToken = checked.activate(activeCount)
			if (activatedToken.status != QueueToken.Status.ACTIVE) {
				throw TokenNotAvailable("현재 대기 중이므로 좌석을 예약할 수 없습니다.")
			}
			queueTokenRepository.save(activatedToken)
			return activatedToken
		}

		// 기본 반환
		return checked
	}
}