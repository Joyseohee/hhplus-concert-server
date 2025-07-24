package kr.hhplus.be.server.service

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.QueueToken
import kr.hhplus.be.server.domain.QueueTokenRepository
import kr.hhplus.be.server.domain.UserBalanceRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class GetQueueTokenService(
	private val userBalanceRepository: UserBalanceRepository,
	private val queueTokenRepository: QueueTokenRepository
) {
	fun getToken(token: String): Output {
		val (queueToken, position) = queueTokenRepository.findTokenWithPosition(token)
			?: throw IllegalArgumentException("토큰을 찾을 수 없습니다.")

		// 만료 체크 및 상태 갱신
		val checkedToken = queueToken.expireIfNeeded()
		if (checkedToken.status == QueueToken.Status.EXPIRED && queueToken.status != QueueToken.Status.EXPIRED) {
			queueTokenRepository.save(checkedToken)
		}

		userBalanceRepository.findById(queueToken.userId)
			?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")

		checkedToken.isValid()

		return Output(
			status = checkedToken.status.name,
			position = position,
			expiresAt = checkedToken.expiresAt
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