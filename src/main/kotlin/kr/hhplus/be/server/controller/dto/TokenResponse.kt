package kr.hhplus.be.server.controller.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "토큰 발급 응답 DTO")
data class TokenResponse (
	@Schema(description = "토큰", example = "abcac10b-58cc-4372-a567-0e02b2c3d479")
	val token: String,
	@Schema(description = "토큰 상태", example = "WAITING")
	val status: String,
	@Schema(description = "대기 순번", example = "1")
	val position: Int,
	@Schema(description = "토큰 만료 일시)", example = "2025-07-20T19:12:34Z")
	val expiresAt: Instant,
)
