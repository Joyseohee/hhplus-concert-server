package kr.hhplus.be.server.service

import kr.hhplus.be.server.domain.QueueTokenRepository
import kr.hhplus.be.server.domain.SeatHoldRepository
import kr.hhplus.be.server.domain.QueueToken
import kr.hhplus.be.server.domain.SeatHold
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
    fun expireStatuses() {
        log.info("스케줄러 실행 시점 :: ${Instant.now()}")
        // QueueToken 만료 처리
        val tokens = queueTokenRepository.findAll()
        tokens.forEach { token ->
            val expired = token.expireIfNeeded()
            if (expired.status == QueueToken.Status.EXPIRED && token.status != QueueToken.Status.EXPIRED) {
                queueTokenRepository.save(expired)
            }
        }

	    // SeatHold 만료 처리
        val holds = seatHoldRepository.findAll()
        holds.forEach { hold ->
            val expired = hold.expireIfNeeded()
            if (expired.status == SeatHold.Status.EXPIRED && hold.status != SeatHold.Status.EXPIRED) {
                seatHoldRepository.save(expired)
            }
        }
        log.info("스케줄러 종료 :: ${Instant.now()}")
    }
}

