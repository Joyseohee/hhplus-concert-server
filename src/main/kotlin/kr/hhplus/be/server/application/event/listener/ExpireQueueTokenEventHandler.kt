package kr.hhplus.be.server.application.event.listener

import kr.hhplus.be.server.application.ExpireQueueTokenUseCase
import kr.hhplus.be.server.domain.model.ConfirmReservationEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener


@Component
class ExpireQueueTokenEventHandler(
	private val expireQueueTokenUseCase: ExpireQueueTokenUseCase,
) {
	@Async("eventExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	fun handle(event: ConfirmReservationEvent) {
		expireQueueTokenUseCase.execute(event.userId)
	}

}