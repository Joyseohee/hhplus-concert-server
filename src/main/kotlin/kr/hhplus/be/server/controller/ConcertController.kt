package kr.hhplus.be.server.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kr.hhplus.be.server.controller.dto.TokenDecodedResponse
import kr.hhplus.be.server.controller.dto.TokenResponse
import kr.hhplus.be.server.support.Response
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

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

}