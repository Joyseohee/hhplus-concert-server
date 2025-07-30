package kr.hhplus.be.server.application

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RequestQueueTokenUseCase(
	private val queueTokenRepository: QueueTokenRepository
) {
	fun createToken(userId: Long): Output {
		val newToken = QueueToken.create(userId = userId)

		val activeCount = queueTokenRepository.countByStatus(QueueToken.Status.ACTIVE)

		val activatedIfPossibleToken = newToken.activate(activeCount + 1)

		val token = queueTokenRepository.save(activatedIfPossibleToken)

		val position = queueTokenRepository.findPositionByToken(token.token)

		return Output(
			token = token.token,
			status = token.status.name,
			position = position ?: 0,
			expiresAt = token.expiresAt
		)
	}

	@Schema(description = "토큰 발급 응답 DTO")
	data class Output(
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

