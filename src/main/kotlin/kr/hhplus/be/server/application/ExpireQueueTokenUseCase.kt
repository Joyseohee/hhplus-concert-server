package kr.hhplus.be.server.application

import jakarta.transaction.Transactional
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import org.springframework.stereotype.Service

@Service
class ExpireQueueTokenUseCase(
	private val queueTokenRepository: QueueTokenRepository,
) {

	@Transactional
	fun execute(userId: Long) {
		val queueToken = queueTokenRepository.findByUserId(userId)
			?: throw IllegalArgumentException("사용자 토큰을 찾을 수 없습니다. 사용자 ID: $userId")

		queueTokenRepository.deleteById(queueToken.userId)
	}
}