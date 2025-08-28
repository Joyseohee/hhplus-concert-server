package kr.hhplus.be.server.application.event.publisher

import kr.hhplus.be.server.domain.model.ConfirmReservationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronizationManager

@Component
class ConfirmReservationEventPublisher(
	private val publisher: ApplicationEventPublisher
) {
	fun publish(event: ConfirmReservationEvent) {
		check(TransactionSynchronizationManager.isActualTransactionActive()) {
			"Publishing a DomainEvent requires an active transaction."
		}

		publisher.publishEvent(event)
	}
}