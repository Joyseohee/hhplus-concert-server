// package kr.hhplus.be.server.controller.swagger
//
// import io.swagger.v3.oas.annotations.Operation
// import io.swagger.v3.oas.annotations.Parameter
// import io.swagger.v3.oas.annotations.enums.ParameterIn
// import io.swagger.v3.oas.annotations.media.Content
// import io.swagger.v3.oas.annotations.media.ExampleObject
// import io.swagger.v3.oas.annotations.media.Schema
// import kr.hhplus.be.server.controller.dto.TokenDecodedResponse
// import kr.hhplus.be.server.controller.dto.TokenResponse
// import kr.hhplus.be.server.support.ApiResponse
// import org.springframework.http.ResponseEntity
// import org.springframework.web.bind.annotation.RequestHeader
// import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
//
// interface SwaggerQueueTokenController {
//
// 	@Operation(summary = "대기열 토큰 발급", tags = ["Queue"])
// 	@SwaggerApiResponse(responseCode = "201", description = "토큰 발급 성공",
// 		content = [ Content(mediaType = "application/json",
// 			schema = Schema(implementation = ApiResponse::class),
// 			examples = [ ExampleObject(
// 				"""
//           {
//             "code": "SUCCESS",
//             "message": "토큰이 성공적으로 발급되었습니다.",
//             "data": {
//               "token": "abcac10b-58cc-4372-a567-0e02b2c3d479",
// 			  "status": "ACTIVE",
// 			  "position": 0,
// 			  "expiresAt": "2025-07-20T19:12:34Z"
//             }
//           }
//           """
// 			) ]
// 		) ]
// 	)
// 	@Parameter(
// 		name        = "X-Client-Id",
// 		`in`        = ParameterIn.HEADER,
// 		description = "클라이언트 식별자",
// 		required    = true,
// 		schema      = Schema(
// 			type    = "string",
// 			format  = "uuid",
// 			example = "f47ac10b-58cc-4372-a567-0e02b2c3d479"
// 		)
// 	)
// 	fun createToken(
// 		userId: Long
// 	): ResponseEntity<ApiResponse<TokenResponse>>
//
// 	@Operation(summary = "대기 번호 및 토큰 상태 조회", tags = ["Queue"])
// 	@SwaggerApiResponse(responseCode = "200", description = "토큰 ACTIVE 혹은 WAITING 상태",
// 		content = [ Content(mediaType = "application/json",
// 			schema = Schema(implementation = ApiResponse::class),
// 			examples = [ ExampleObject(
// 				"""
//           {
//             "code": "SUCCESS",
//             "message": "토큰 상태 조회 성공",
//             "data": {
//               "status": "ACTIVE",
//               "position": 0,
//               "expiresAt": "2025-07-20T19:12:34Z"
//             }
//           }
//           """
// 			) ]
// 		) ]
// 	)
// 	@Parameter(
// 		name        = "X-Queue-Token",
// 		`in`        = ParameterIn.HEADER,
// 		description = "대기열 토큰",
// 		required    = true,
// 		schema      = Schema(
// 			type    = "string",
// 			format  = "encoded",
// 			example = "abcac10b-58cc-4372-a567-0e02b2c3d479"
// 		)
// 	)
// 	fun getTokenStatus(
// 		token: String
// 	): ResponseEntity<ApiResponse<TokenDecodedResponse>>
// }
