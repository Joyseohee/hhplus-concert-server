package kr.hhplus.be.server.infrastructure.message.kafka.scheduler

import kr.hhplus.be.server.application.ActivateQueueTokenUseCase
import kr.hhplus.be.server.domain.model.QueueToken.Companion.ACTIVATE_BATCH_SIZE
import kr.hhplus.be.server.infrastructure.message.kafka.listener.QueueListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class QueueScheduler(
	private val queueListener: QueueListener,
	private val service: ActivateQueueTokenUseCase,
) {
	@Scheduled(fixedRate = 5000)
	fun processTokens() {
		repeat(ACTIVATE_BATCH_SIZE) {
			val pending = queueListener.poll() ?: return
			try {
				service.execute(pending.payload.userId)
				pending.ack.acknowledge() // 처리 성공 후 커밋
			} catch (ex: Exception) {
				println("Failed to activate token: ${ex.message}")
			}
		}

	}
}
