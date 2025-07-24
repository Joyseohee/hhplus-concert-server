// package kr.hhplus.be.server.controller
//
// import kr.hhplus.be.server.controller.dto.TokenDecodedResponse
// import kr.hhplus.be.server.controller.dto.TokenResponse
// import kr.hhplus.be.server.controller.swagger.SwaggerQueueTokenController
// import kr.hhplus.be.server.support.ApiResponse
// import org.springframework.http.HttpStatus
// import org.springframework.http.ResponseEntity
// import org.springframework.web.bind.annotation.GetMapping
// import org.springframework.web.bind.annotation.PostMapping
// import org.springframework.web.bind.annotation.RequestHeader
// import org.springframework.web.bind.annotation.RequestMapping
// import org.springframework.web.bind.annotation.RestController
// import java.time.Instant
// import java.util.*
//
// @RestController
// @RequestMapping("/api/v1/queue/token")
// class QueueTokenController : SwaggerQueueTokenController {
//
// 	@PostMapping("/")
// 	override fun createToken(
// 		@RequestHeader(name = "X-Client-Id", required = true) userId: Long
// 	): ResponseEntity<ApiResponse<TokenResponse>> {
// 		// 실제 비즈니스 로직 구현부
// 		return ResponseEntity.status(HttpStatus.CREATED).body(
// 			ApiResponse(
// 				code = "SUCCESS",
// 				message = "토큰 발급 성공",
// 				data = TokenResponse("token", "ACTIVE", 0, Instant.now())
// 			)
// 		)
// 	}
//
// 	@GetMapping("/")
// 	override fun getTokenStatus(
// 		@RequestHeader(name = "X-Queue-Token", required = true) token: String
// 	): ResponseEntity<ApiResponse<TokenDecodedResponse>> {
// 		// 비즈니스 로직 구현
// 		return ResponseEntity.ok(
// 			ApiResponse(
// 				code = "SUCCESS",
// 				message = "토큰 상태 조회 성공",
// 				data = TokenDecodedResponse("ACTIVE", 0, Instant.now())
// 			)
// 		)
// 	}
// }
