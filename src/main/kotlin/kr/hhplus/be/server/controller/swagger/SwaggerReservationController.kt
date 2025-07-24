package kr.hhplus.be.server.controller.swagger

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.service.ConfirmReservationService
import kr.hhplus.be.server.service.HoldSeatService
import kr.hhplus.be.server.service.ListConcertService
import kr.hhplus.be.server.service.ListSeatService
import kr.hhplus.be.server.support.ApiResponse
import org.springframework.http.ResponseEntity
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

interface SwaggerReservationController {
	@Operation(summary = "예약 가능한 콘서트 목록 조회", tags = ["Reservations"])
	@SwaggerApiResponse(
		responseCode = "200", description = "조회 성공",
		content = [Content(
			mediaType = "application/json",
			schema = Schema(implementation = ApiResponse::class),
			examples = [ExampleObject(
				"""
          {
            "code": "SUCCESS",
            "message": "요청이 성공했습니다.",
            "data": [
              {
                "concertId": 1,
                "concertDateTime": "2023-10-01T19:00:00",
                "concertVenue": "서울 올림픽공원 체조경기장",
                "concertTitle": "2023 HH+ Concert",
                "isAvailable": true
              },
              {
                "concertId": 2,
                "concertDateTime": "2023-10-02T19:00:00",
                "concertVenue": "서울 올림픽공원 주경기장",
                "concertTitle": "2023 Jazz Concert",
                "isAvailable": false
              }
            ]
          }
          """
			)]
		)]
	)
	@Parameter(
		name = "X-Queue-Token",
		`in` = ParameterIn.HEADER,
		description = "대기열 토큰",
		required = true,
		schema = Schema(
			type = "string",
			format = "encoded",
			example = "abcac10b-58cc-4372-a567-0e02b2c3d479"
		)
	)
	fun getConcerts(
		token: String
	): ResponseEntity<ApiResponse<ListConcertService.Output>>

	@Operation(summary = "특정 콘서트의 좌석 목록 조회", tags = ["Reservations"])
	@SwaggerApiResponse(responseCode = "200", description = "조회 성공",
		content = [ Content(mediaType = "application/json",
			schema = Schema(implementation = ApiResponse::class),
			examples = [ ExampleObject(
				"""
          {
            "code": "SUCCESS",
            "message": "요청이 성공했습니다.",
            "data": {
              "concertId": 1,
              "availableSeats": [
                { "seatId": 1, "seatNumber":1, "price": 130000, "isAvailable": true },
                { "seatId": 2, "seatNumber":2, "price": 150000, "isAvailable": false }
              ]
            }
          }
          """
			) ]
		) ]
	)
	@Parameter(
		name        = "X-Queue-Token",
		`in`        = ParameterIn.HEADER,
		description = "대기열 토큰",
		required    = true,
		schema      = Schema(
			type    = "string",
			format  = "encoded",
			example = "abcac10b-58cc-4372-a567-0e02b2c3d479"
		)
	)
	fun getSeats(
		token: String,
		concertId: Long
	): ResponseEntity<ApiResponse<ListSeatService.Output>>

	@Operation(summary = "좌석 점유 요청", tags = ["Reservations"])
	@SwaggerApiResponse(
		responseCode = "200", description = "점유 성공",
		content = [Content(
			mediaType = "application/json",
			schema = Schema(implementation = ApiResponse::class),
			examples = [ExampleObject(
				"""
          {
            "code": "SUCCESS",
            "message": "좌석 점유 성공",
            "data": {
              "seatId": 1,
              "seatNumber": 1,
              "expiresAt": "2025-07-20T19:12:34Z"
            }
          }
          """
			)]
		)]
	)
	@Parameter(
		name        = "X-Queue-Token",
		`in`        = ParameterIn.HEADER,
		description = "대기열 토큰",
		required    = true,
		schema      = Schema(
			type    = "string",
			format  = "encoded",
			example = "abcac10b-58cc-4372-a567-0e02b2c3d479"
		)
	)
	fun holdSeats(
		token: String,
		seatsHoldRequest: HoldSeatService.Input
	): ResponseEntity<ApiResponse<HoldSeatService.Output>>


	@Operation(summary = "결제 및 예약 확정", tags = ["Reservations"])
	@SwaggerApiResponse(responseCode = "200", description = "결제 성공",
		content = [ Content(mediaType = "application/json",
			schema = Schema(implementation = ApiResponse::class),
			examples = [ ExampleObject(
				"""
          {
            "code": "SUCCESS",
            "message": "예약이 성공적으로 완료되었습니다.",
            "data": {
              "concertId": 1,
              "seatId": 1,
              "price": 130000
            }
          }
          """
			) ]
		) ]
	)
	@Parameter(
		name        = "X-Queue-Token",
		`in`        = ParameterIn.HEADER,
		description = "대기열 토큰",
		required    = true,
		schema      = Schema(
			type    = "string",
			format  = "encoded",
			example = "abcac10b-58cc-4372-a567-0e02b2c3d479"
		)
	)
	fun confirmedReservation(
		token: String,
		reservationRequest: ConfirmReservationService.Input
	): ResponseEntity<ApiResponse<ConfirmReservationService.Output>>
}