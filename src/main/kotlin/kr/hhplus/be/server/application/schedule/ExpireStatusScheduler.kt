package kr.hhplus.be.server.application.schedule

import jakarta.transaction.Transactional
import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.model.QueueToken.Companion.MAX_ACTIVE_COUNT
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import kr.hhplus.be.server.domain.repository.SeatHoldRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ExpireStatusScheduler(
	private val queueTokenRepository: QueueTokenRepository,
	private val seatHoldRepository: SeatHoldRepository
) {
	private val log = LoggerFactory.getLogger(javaClass)

	@Scheduled(fixedDelay = 5_000L)
	@Transactional
	fun expireStatuses() {
		log.info("스케줄러 실행 시점 :: ${Instant.now()}")
		// QueueToken 만료 처리
		val now = Instant.now()
		queueTokenRepository.deleteExpired(now)

		// SeatHold 만료 처리
		val holds = seatHoldRepository.findHoldsToExpire()
		seatHoldRepository.deleteByIds(holds.map { it.seatHoldId!! })
		log.info("스케줄러 종료 :: ${Instant.now()}")
	}
}