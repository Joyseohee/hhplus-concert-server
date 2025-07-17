package kr.hhplus.be.server.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "잔액 응답 DTO")
data class BalanceResponse(
	@Schema(description = "잔액", example = "300000")
	val balance: Int
)
