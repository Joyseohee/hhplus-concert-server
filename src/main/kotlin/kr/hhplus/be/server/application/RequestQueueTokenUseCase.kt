package kr.hhplus.be.server.application

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.transaction.Transactional
import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import kr.hhplus.be.server.infrastructure.message.kafka.dto.QueueEnteredMessage
import kr.hhplus.be.server.infrastructure.message.kafka.producer.QueueProducer
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RequestQueueTokenUseCase(
	private val queueProducer: QueueProducer,
	private val queueTokenRepository: QueueTokenRepository
) {
	@Transactional
	fun createToken(userId: Long): Output {
		val newToken = QueueToken.create(userId = userId)
		val token = queueTokenRepository.save(newToken)

		queueProducer.send(QueueEnteredMessage(
			userId = userId,
		))

		return Output(
			token = token.token,
			status = token.status.name,
			position = token.position,
			expiresAt = token.expiresAt!!
		)
	}

	@Schema(description = "토큰 발급 응답 DTO")
	data class Output(
		@Schema(description = "토큰", example = "queue:waiting:1234567890")
		val token: String,
		@Schema(description = "토큰 상태", example = "WAITING")
		val status: String,
		@Schema(description = "대기 순번", example = "1")
		val position: Int,
		@Schema(description = "토큰 만료 일시)", example = "2025-07-20T19:12:34Z")
		val expiresAt: Instant,
	)
}

