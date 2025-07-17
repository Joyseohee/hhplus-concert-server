package kr.hhplus.be.server.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용 가능한 날짜 응답 DTO")
data class AvailableDatesResponse(
	@Schema(description = "콘서트 목록")
	val availableDates: List<AvailableDatesResponseItem>
)


data class AvailableDatesResponseItem(
	@Schema(description = "콘서트 id", example = "1")
	val concertId: Long,
	@Schema(description = "콘서트 일시", example = "2023-10-01T19:00:00")
	val concertDateTime: String,
	@Schema(description = "콘서트 장소", example = "서울 올림픽공원 체조경기장")
	val concertVenue: String,
	@Schema(description = "콘서트 제목", example = "2023 HH+ Concert")
	val concertTitle: String,
	@Schema(description = "예약 가능 여부", example = "true")
	val isAvailable: Boolean,
)