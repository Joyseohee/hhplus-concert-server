package kr.hhplus.be.server.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "좌석 점유 요청 DTO")
data class SeatsHoldRequest(
	@Schema(description = "점유 요청 ID", example = "0e02b2c3d479", requiredMode = Schema.RequiredMode.REQUIRED)
	val seatHoldId: String,
	@Schema(description = "좌석 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	val seatId: Long
)
