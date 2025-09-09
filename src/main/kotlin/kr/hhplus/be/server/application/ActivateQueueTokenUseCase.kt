package kr.hhplus.be.server.application

import jakarta.transaction.Transactional
import kr.hhplus.be.server.domain.model.QueueToken.Companion.ACTIVATE_BATCH_SIZE
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ActivateQueueTokenUseCase(
	private val queueTokenRepository: QueueTokenRepository
) {
	@Transactional
	fun execute(userId: Long) {
		// 만료된 수만큼 activate 상태로 변경
		queueTokenRepository.activate(userId, Instant.now())
	}
}

