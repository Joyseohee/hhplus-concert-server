package kr.hhplus.be.server.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kr.hhplus.be.server.controller.dto.AvailableDatesResponse
import kr.hhplus.be.server.controller.dto.AvailableDatesResponseItem
import kr.hhplus.be.server.controller.dto.AvailableSeatsResponse
import kr.hhplus.be.server.controller.dto.AvailableSeatsResponseItem
import kr.hhplus.be.server.controller.dto.BalanceRequest
import kr.hhplus.be.server.controller.dto.BalanceResponse
import kr.hhplus.be.server.controller.dto.ReservationRequest
import kr.hhplus.be.server.controller.dto.ReservationResponse
import kr.hhplus.be.server.controller.dto.SeatsHoldRequest
import kr.hhplus.be.server.controller.dto.SeatsHoldResponse
import kr.hhplus.be.server.controller.dto.TokenDecodedResponse
import kr.hhplus.be.server.controller.dto.TokenResponse
import kr.hhplus.be.server.support.Response
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import kotlin.ranges.contains

@RestController
@RequestMapping("/")
class ConcertController {

	@Operation(summary = "대기열 토큰 발급", tags = ["Queue"])
	@ApiResponses(
		ApiResponse(responseCode = "201", description = "토큰 발급 성공",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "SUCCESS",
            "message": "토큰이 성공적으로 발급되었습니다.",
            "data": {
              "token": "abcac10b-58cc-4372-a567-0e02b2c3d479"
            }
          }
          """
				) ]
			) ]
		),
		ApiResponse(responseCode = "404", description = "유효하지 않은 사용자",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "INVALID_USER",
            "message": "유효하지 않은 사용자입니다.",
            "data": null
          }
          """
				) ]
			) ]
		)
	)
	@Parameter(
		name        = "X-Client-Id",
		`in`        = ParameterIn.HEADER,
		description = "클라이언트 식별자(UUID)",
		required    = true,
		schema      = Schema(
			type    = "string",
			format  = "uuid",
			example = "f47ac10b-58cc-4372-a567-0e02b2c3d479"
		)
	)
	@PostMapping("/queue/token")
	fun createToken(
		@RequestHeader(name = "X-Client-Id", required = true) userId: UUID
	): ResponseEntity<Response<TokenResponse>> {
		val isValid = userId == UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479")

		return if (isValid) {
			return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(
					Response(
						code = "SUCCESS",
						message = "토큰이 성공적으로 발급되었습니다.",
						data = TokenResponse("abcac10b-58cc-4372-a567-0e02b2c3d479")
					)
				)
		} else {
			ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(
					Response(
						code = "INVALID_USER",
						message = "유효하지 않은 사용자입니다.",
						data = null
					)
				)
		}
	}

	@Operation(summary = "대기 번호 및 토큰 상태 조회", tags = ["Queue"])
	@ApiResponses(
		ApiResponse(responseCode = "200", description = "토큰 ACTIVE 혹은 WAITING 상태",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "SUCCESS",
            "message": "토큰 상태 조회 성공",
            "data": {
              "status": "ACTIVE",
              "position": 0,
              "remainingTimeMills": 300000
            }
          }
          """
				) ]
			) ]
		),
		ApiResponse(responseCode = "410", description = "토큰 만료",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "EXPIRED",
            "message": "토큰이 만료되었습니다.",
            "data": null
          }
          """
				) ]
			) ]
		),
		ApiResponse(responseCode = "404", description = "유효하지 않은 토큰",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "INVALID_TOKEN",
            "message": "유효하지 않은 토큰입니다.",
            "data": null
          }
          """
				) ]
			) ]
		)
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
	@GetMapping("/queue/token")
	fun getTokenStatus(
		@RequestHeader(name = "X-Queue-Token", required = true) token: String
	): ResponseEntity<Response<TokenDecodedResponse>> {
		val isActive = token == "abcac10b-58cc-4372-a567-0e02b2c3d479"
		val isWaiting = token == "bbcac10b-58cc-4372-a567-0e02b2c3d222"
		val isExpired = token == "bbcac10b-aaaa-4372-a567-0e02b2c3d222"
		return if (isActive) {
			ResponseEntity.status(HttpStatus.OK)
				.body(
					Response(
						code = "SUCCESS",
						message = "토큰 상태 조회 성공",
						data = TokenDecodedResponse(
							status = "ACTIVE",
							position = 0,
							remainingTimeMills = 300000L
						)
					)
				)
		} else if (isWaiting) {
			ResponseEntity.status(HttpStatus.OK)
				.body(
					Response(
						code = "SUCCESS",
						message = "대기 중인 토큰입니다.",
						data = TokenDecodedResponse(
							status = "WAITING",
							position = 5,
							remainingTimeMills = 600000L
						)
					)
				)
		} else if (isExpired) {
			ResponseEntity
				.status(HttpStatus.GONE)
				.body(
					Response(
						code = "EXPIRED",
						message = "토큰이 만료되었습니다.",
						data = TokenDecodedResponse(
							status = "EXPIRED",
							position = 0,
							remainingTimeMills = 0L
						)
					)
				)
		} else {
			ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(
					Response(
						code = "INVALID_TOKEN",
						message = "유효하지 않은 토큰입니다.",
						data = null
					)
				)
		}
	}


	@Operation(summary = "예약 가능한 날짜(콘서트) 목록 조회", tags = ["Seat"])
	@ApiResponses(
		ApiResponse(responseCode = "200", description = "조회 성공",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
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
				) ]
			) ]
		),
		ApiResponse(responseCode = "410", description = "토큰 만료",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "EXPIRED",
            "message": "토큰이 만료되었습니다.",
            "data": null
          }
          """
				) ]
			) ]
		),
		ApiResponse(responseCode = "404", description = "유효하지 않은 토큰",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "INVALID_TOKEN",
            "message": "유효하지 않은 토큰입니다.",
            "data": null
          }
          """
				) ]
			) ]
		)
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
	@GetMapping("/schedules/available-dates")
	fun getAvailableDates(
		@RequestHeader(name = "X-Queue-Token", required = true) token: String
	): ResponseEntity<Response<AvailableDatesResponse>> {
		val isActive = token == "abcac10b-58cc-4372-a567-0e02b2c3d479"
		val isExpired = token == "bbcac10b-aaaa-4372-a567-0e02b2c3d222"

		return if (isActive) {
			ResponseEntity.ok(
				Response(
					code = "SUCCESS",
					message = "요청이 성공했습니다.",
					data = AvailableDatesResponse(
						listOf(
							AvailableDatesResponseItem(
								concertId = 1L,
								concertDateTime = "2023-10-01T19:00:00",
								concertVenue = "서울 올림픽공원 체조경기장",
								concertTitle = "2023 HH+ Concert",
								isAvailable = true
							),
							AvailableDatesResponseItem(
								concertId = 2L,
								concertDateTime = "2023-10-02T19:00:00",
								concertVenue = "서울 올림픽공원 주경기장",
								concertTitle = "2023 Jazz Concert",
								isAvailable = false
							)
						)

					)
				)
			)
		} else if (isExpired) {
			ResponseEntity
				.status(HttpStatus.GONE)
				.body(
					Response(
						code = "EXPIRED",
						message = "토큰이 만료되었습니다.",
					)
				)
		} else {
			ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(
					Response(
						code = "INVALID_TOKEN",
						message = "유효하지 않은 토큰입니다.",
						data = null
					)
				)
		}
	}

	@Operation(summary = "특정 콘서트의 좌석 목록 조회", tags = ["Seat"])
	@ApiResponses(
		ApiResponse(responseCode = "200", description = "조회 성공",
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
                { "seatId": 1, "price": 130000, "isAvailable": true },
                { "seatId": 2, "price": 150000, "isAvailable": false }
              ]
            }
          }
          """
				) ]
			) ]
		),
		ApiResponse(responseCode = "410", description = "토큰 만료",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "EXPIRED",
            "message": "토큰이 만료되었습니다."
          }
          """
				) ]
			) ]
		),
		ApiResponse(responseCode = "404", description = "유효하지 않은 토큰 or 콘서트 ID",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "INVALID_TOKEN",
            "message": "유효하지 않은 토큰입니다."
          }
          """
				) ]
			) ]
		)
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
	@GetMapping("/schedules/{concertId}/available-seats")
	fun getAvailableSeats(
		@RequestHeader(name = "X-Queue-Token", required = true) token: String,

		@PathVariable concertId: Long
	): ResponseEntity<Response<AvailableSeatsResponse>> {
		val isValid = token == "abcac10b-58cc-4372-a567-0e02b2c3d479" && concertId == 1L
		val isExpired = token == "bbcac10b-aaaa-4372-a567-0e02b2c3d222"

		return if (isValid) {
			ResponseEntity.ok(
				Response(
					code = "SUCCESS",
					message = "요청이 성공했습니다.",
					data = AvailableSeatsResponse(
						concertId = concertId,
						availableDates = listOf(
							AvailableSeatsResponseItem(
								seatId = 1L,
								price = "130000",
								isAvailable = true
							),
							AvailableSeatsResponseItem(
								seatId = 2L,
								price = "150000",
								isAvailable = false
							)
						)
					)
				)
			)
		} else if (isExpired) {
			ResponseEntity
				.status(HttpStatus.GONE)
				.body(
					Response(
						code = "EXPIRED",
						message = "토큰이 만료되었습니다.",
					)
				)
		} else {
			ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(
					Response(
						code = "INVALID_TOKEN",
						message = "유효하지 않은 토큰입니다.",
						data = null
					)
				)
		}
	}

	@Operation(summary = "좌석 점유 요청", tags = ["Seat"])
	@ApiResponses(
		ApiResponse(responseCode = "200", description = "점유 성공",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "SUCCESS",
            "message": "좌석 점유 성공",
            "data": {
              "seatId": 1,
              "remainingTimeMills": 300000
            }
          }
          """
				) ]
			) ]
		),
		ApiResponse(responseCode = "400", description = "유효하지 않은 좌석",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "INVALID_SEAT",
            "message": "유효하지 않은 좌석입니다."
          }
          """
				) ]
			) ]
		),
		ApiResponse(responseCode = "404", description = "유효하지 않은 토큰",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "INVALID_TOKEN",
            "message": "유효하지 않은 토큰입니다."
          }
          """
				) ]
			) ]
		),
		ApiResponse(responseCode = "410", description = "토큰 만료",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "EXPIRED",
            "message": "토큰이 만료되었습니다."
          }
          """
				) ]
			) ]
		)
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
	@PostMapping("/schedules/{concertId}/seats/hold")
	fun holdSeats(
		@RequestHeader(name = "X-Queue-Token", required = true) token: String,

		@RequestBody seatsHoldRequest: SeatsHoldRequest
	): ResponseEntity<Response<SeatsHoldResponse>> {
		val isValid = token == "abcac10b-58cc-4372-a567-0e02b2c3d479" && seatsHoldRequest.seatId in 1..50
		val isInvalid = token == "bbcac10b-58cc-4372-a567-0e02b2c3d222"
		val isExpired = token == "bbcac10b-aaaa-4372-a567-0e02b2c3d222"

		return if (isValid) {
			ResponseEntity
				.status(HttpStatus.OK)
				.body(
					Response(
						code = "SUCCESS",
						message = "요청이 성공했습니다.",
						data = SeatsHoldResponse(
							seatId = seatsHoldRequest.seatId,
							remainingTimeMills = 300000
						)
					)
				)
		} else if (isInvalid) {
			ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(
					Response(
						code = "INVALID_TOKEN",
						message = "유효하지 않은 토큰입니다.",
						data = null
					)
				)
		} else if (isExpired) {
			ResponseEntity
				.status(HttpStatus.GONE)
				.body(
					Response(
						code = "EXPIRED",
						message = "토큰이 만료되었습니다.",
					)
				)
		} else {
			ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(
					Response(
						code = "INVALID_SEAT",
						message = "유효하지 않은 좌석입니다.",
						data = null
					)
				)
		}
	}

	@Operation(summary = "잔액 조회", tags = ["Balance"])
	@ApiResponses(
		ApiResponse(responseCode = "200", description = "조회 성공",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "SUCCESS",
            "message": "잔액 조회 성공",
            "data": { "balance": 50000 }
          }
          """
				) ]
			) ]
		),
		ApiResponse(responseCode = "400", description = "토큰 검증 실패",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "BAD_REQUEST",
            "message": "토큰 검증 실패"
          }
          """
				) ]
			) ]
		)
	)
	@Parameter(
		name        = "X-Client-Id",
		`in`        = ParameterIn.HEADER,
		description = "클라이언트 식별자(UUID)",
		required    = true,
		schema      = Schema(
			type    = "string",
			format  = "uuid",
			example = "f47ac10b-58cc-4372-a567-0e02b2c3d479"
		)
	)
	@GetMapping("/balance")
	fun getBalance(
		@RequestHeader(name = "X-Client-Id", required = true) userId: UUID,
	): ResponseEntity<Response<BalanceResponse>> {
		val isValid = userId == UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479")

		return if (isValid) {
			ResponseEntity.status(HttpStatus.OK).body(
				Response(
					code = "SUCCESS",
					message = "잔액 조회 성공",
					data = BalanceResponse(balance = 50000)
				)
			)
		} else {
			ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(
					Response(
						code = "BAD_REQUEST",
						message = "잔액 조회 실패",
						data = null
					)
				)
		}
	}

	@Operation(summary = "잔액 충전", tags = ["Balance"])
	@ApiResponses(
		ApiResponse(responseCode = "200", description = "충전 성공",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "SUCCESS",
            "message": "잔액 충전 성공",
            "data": { "balance": 80000 }
          }
          """
				) ]
			) ]
		),
		ApiResponse(responseCode = "400", description = "충전 실패",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "BAD_REQUEST",
            "message": "충전 실패"
          }
          """
				) ]
			) ]
		)
	)
	@Parameter(
		name        = "X-Client-Id",
		`in`        = ParameterIn.HEADER,
		description = "클라이언트 식별자(UUID)",
		required    = true,
		schema      = Schema(
			type    = "string",
			format  = "uuid",
			example = "f47ac10b-58cc-4372-a567-0e02b2c3d479"
		)
	)
	@PostMapping("/balance/charge")
	fun chargeBalance(
		@RequestHeader(name = "X-Client-Id", required = true) userId: UUID,
		@RequestBody balanceRequest: BalanceRequest
	): ResponseEntity<Response<BalanceResponse>> {
		val isValid = userId == UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479")

		return if (isValid) {
			ResponseEntity.status(HttpStatus.OK).body(
				Response(
					code = "SUCCESS",
					message = "잔액 충전 성공",
					data = BalanceResponse(balance = 50000)
				)
			)
		} else {
			ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(
					Response(
						code = "BAD_REQUEST",
						message = "잔액 충전 실패",
						data = null
					)
				)
		}
	}

	@Operation(summary = "결제 및 예약 확정", tags = ["Reservations"])
	@ApiResponses(
		ApiResponse(responseCode = "200", description = "결제 성공",
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
		),
		ApiResponse(responseCode = "400", description = "잔액 부족 or 잘못된 요청",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "NO_BALANCE",
            "message": "잔액이 부족합니다."
          }
          """
				) ]
			) ]
		),
		ApiResponse(responseCode = "404", description = "유효하지 않은 토큰",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "INVALID_TOKEN",
            "message": "유효하지 않은 토큰입니다."
          }
          """
				) ]
			) ]
		),
		ApiResponse(responseCode = "410", description = "토큰 또는 좌석 점유 만료",
			content = [ Content(mediaType = "application/json",
				schema = Schema(implementation = ApiResponse::class),
				examples = [ ExampleObject(
					"""
          {
            "code": "EXPIRED_TOKEN",
            "message": "토큰이 만료되었습니다."
          }
          """
				) ]
			) ]
		)
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
	@PostMapping("/reservations")
	fun confirmedReservation(
		@RequestHeader(name = "X-Queue-Token", required = true) token: String,

		@RequestBody reservationRequest: ReservationRequest
	): ResponseEntity<Response<ReservationResponse>> {
		val isValid = token == "abcac10b-58cc-4372-a567-0e02b2c3d479" && reservationRequest.seatHoldId == "0e02b2c3d479"
		val isInvalidToken = token == "bbcac10b-58cc-4372-a567-0e02b2c3d222"
		val isInvalidBalance = token == "bbcac10b-58cc-4372-a567-0e02b2c3d223"
		val isExpiredToken = token == "bbcac10b-aaaa-4372-a567-0e02b2c3d222"
		val isExpiredSeats = token == "bbcac10b-aaaa-4372-a567-0e02b2c3d122"

		return if (isValid) {
			ResponseEntity.status(HttpStatus.OK).body(
				Response(
					code = "SUCCESS",
					message = "예약이 성공적으로 완료되었습니다.",
					data = ReservationResponse(
						concertId = 1L,
						seatId = 1L,
						price = 130000,
					)
				)
			)
		} else if (isInvalidToken) {
			ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(
					Response(
						code = "INVALID_TOKEN",
						message = "유효하지 않은 토큰입니다.",
						data = null
					)
				)
		} else if (isInvalidBalance) {
			ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(
					Response(
						code = "NO_BALANCE",
						message = "잔액이 부족합니다.",
						data = null
					)
				)
		} else if (isExpiredToken) {
			ResponseEntity
				.status(HttpStatus.GONE)
				.body(
					Response(
						code = "EXPIRED_TOKEN",
						message = "토큰이 만료되었습니다.",
					)
				)
		} else if (isExpiredSeats) {
			ResponseEntity
				.status(HttpStatus.GONE)
				.body(
					Response(
						code = "EXPIRED_SEATS_HOLD",
						message = "좌석 점유가 만료되었습니다.",
					)
				)
		} else {
			ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(
					Response(
						code = "BAD_REQUEST",
						message = "예약 실패",
						data = null
					)
				)
		}
	}
}