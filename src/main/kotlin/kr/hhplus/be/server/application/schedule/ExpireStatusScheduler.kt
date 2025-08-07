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

	// 1분마다 만료 상태 갱신
	@Scheduled(fixedDelay = 60_000)
	@Transactional
	fun expireStatuses() {
		log.info("스케줄러 실행 시점 :: ${Instant.now()}")
		// QueueToken 만료 처리
		val tokens = queueTokenRepository.findTokensToExpire()
		queueTokenRepository.deleteByIds(tokens.map { it.tokenId!! })

		// 만료된 수만큼 activate 상태로 변경
		val activeCount = queueTokenRepository.countByStatus(QueueToken.Status.ACTIVE)
		val forActivate = queueTokenRepository.findAllWaitingTokenForActivate(MAX_ACTIVE_COUNT - activeCount)
		forActivate.forEach { it.activate(activeCount + 1) }
		queueTokenRepository.saveAll(forActivate)

		// SeatHold 만료 처리
		val holds = seatHoldRepository.findHoldsToExpire()
		seatHoldRepository.deleteByIds(holds.map { it.seatHoldId!! })
		log.info("스케줄러 종료 :: ${Instant.now()}")
	}
}