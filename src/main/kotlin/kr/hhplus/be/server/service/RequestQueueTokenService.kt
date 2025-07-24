package kr.hhplus.be.server.service

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.QueueToken
import kr.hhplus.be.server.domain.QueueToken.Companion.MAX_ACTIVE_COUNT
import kr.hhplus.be.server.domain.QueueTokenRepository
import kr.hhplus.be.server.domain.UserBalanceRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RequestQueueTokenService(
	private val userBalanceRepository: UserBalanceRepository,
	private val queueTokenRepository: QueueTokenRepository
) {
	fun createToken(userId: Long): Output? {
		userBalanceRepository.findById(userId)
			?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")

		val activeCount = queueTokenRepository.countByStatus(QueueToken.Status.ACTIVE)

		val newToken = QueueToken.create(userId = userId).apply {
			activate(activeCount)
		}

		val token = queueTokenRepository.save(newToken)

		val position = queueTokenRepository.findPositionByToken(token.token)
			?: throw IllegalArgumentException("토큰 정보를 찾을 수 없습니다.")

		return queueTokenRepository.findById(userId)?.let { queueToken ->
			Output(
				token = queueToken.token,
				status = queueToken.status.name,
				position = position,
				expiresAt = queueToken.expiresAt
			)
		}
	}

	@Schema(description = "토큰 발급 응답 DTO")
	data class Output (
		@Schema(description = "토큰", example = "abcac10b-58cc-4372-a567-0e02b2c3d479")
		val token: String,
		@Schema(description = "토큰 상태", example = "WAITING")
		val status: String,
		@Schema(description = "대기 순번", example = "1")
		val position: Int,
		@Schema(description = "토큰 만료 일시)", example = "2025-07-20T19:12:34Z")
		val expiresAt: Instant,
	)
}

