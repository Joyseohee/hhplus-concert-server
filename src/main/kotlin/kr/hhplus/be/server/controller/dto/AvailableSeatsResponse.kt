package kr.hhplus.be.server.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용 가능한 좌석 응답 DTO")
data class AvailableSeatsResponse(
	@Schema(description = "콘서트 ID", example = "1")
	val concertId: Long,
	@Schema(description = "좌석 목록")
	val availableDates: List<AvailableSeatsResponseItem>
)


data class AvailableSeatsResponseItem(
	@Schema(description = "좌석 id", example = "1")
	val seatId: Long,
	@Schema(description = "좌석 가격", example = "130000")
	val price: String,
	@Schema(description = "예약 가능 여부", example = "true")
	val isAvailable: Boolean,
)