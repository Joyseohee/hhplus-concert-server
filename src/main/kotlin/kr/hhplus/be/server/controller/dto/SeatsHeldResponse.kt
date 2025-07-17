package kr.hhplus.be.server.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "좌석 점유 응답 DTO")
data class SeatsHoldResponse(
	@Schema(description = "좌석 ID", example = "VALID")
	val seatId: Long,
	@Schema(description = "점유 만료까지 남은 시각", example = "300000")
	val remainingTimeMills: Long,
)
