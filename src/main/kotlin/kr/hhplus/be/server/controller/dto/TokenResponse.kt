package kr.hhplus.be.server.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "토큰 발급 응답 DTO")
data class TokenResponse(
	@Schema(description = "토큰", example = "abcac10b-58cc-4372-a567-0e02b2c3d479")
	val token: String,
)
