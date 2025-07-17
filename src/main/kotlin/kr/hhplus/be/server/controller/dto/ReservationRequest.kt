package kr.hhplus.be.server.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "결제 및 예약 확정 응답 DTO")
data class ReservationRequest(
	@Schema(description = "점유 요청 ID", example = "0e02b2c3d479", requiredMode = Schema.RequiredMode.REQUIRED)
	val seatHoldId: String,
)

