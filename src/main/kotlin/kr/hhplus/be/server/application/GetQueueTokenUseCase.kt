package kr.hhplus.be.server.application

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class GetQueueTokenUseCase(
	private val queueTokenRepository: QueueTokenRepository
) {
	fun getToken(token: String): Output {
		val queueToken = queueTokenRepository.findByToken(token)
			?: throw IllegalArgumentException("토큰을 찾을 수 없습니다: $token")

		return Output(
			status = queueToken.status.name,
			position = queueToken.position,
			expiresAt = queueToken.expiresAt!!
		)
	}

	@Schema(description = "토큰 상태 응답 DTO")
	data class Output(
		@Schema(description = "토큰 상태", example = "WAITING")
		val status: String,
		@Schema(description = "대기 순번", example = "1")
		val position: Int,
		@Schema(description = "토큰 만료 일시)", example = "2025-07-20T19:12:34Z")
		val expiresAt: Instant,
	)
}