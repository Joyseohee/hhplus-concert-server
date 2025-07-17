package kr.hhplus.be.server.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "잔액 충전 요청 DTO")
data class BalanceRequest(
	@Schema(description = "충전액", example = "300000", requiredMode = Schema.RequiredMode.REQUIRED)
	val amount: Int
)
