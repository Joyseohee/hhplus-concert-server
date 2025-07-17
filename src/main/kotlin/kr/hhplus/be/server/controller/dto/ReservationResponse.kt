package kr.hhplus.be.server.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "결제 및 예약 확정 응답 DTO")
data class ReservationResponse(
	@Schema(description = "콘서트 ID", example = "1")
	val concertId: Long,
	@Schema(description = "좌석 id", example = "1")
	val seatId: Long,
	@Schema(description = "결제 가격", example = "130000")
	val price: Int,
)

