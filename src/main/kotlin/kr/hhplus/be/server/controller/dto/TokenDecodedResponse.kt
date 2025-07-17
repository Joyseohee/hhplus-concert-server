package kr.hhplus.be.server.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "토큰 상태 응답 DTO")
data class TokenDecodedResponse(
	@Schema(description = "토큰 상태", example = "WAITING")
	val status: String,
	@Schema(description = "대기 순번", example = "1")
	val position: Int,
	@Schema(description = "토큰 만료까지 남은 시간 (밀리초 단위)", example = "300000")
	val remainingTimeMills: Long,
)
