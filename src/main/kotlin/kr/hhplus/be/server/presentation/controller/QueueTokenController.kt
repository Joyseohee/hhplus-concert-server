package kr.hhplus.be.server.presentation.controller

import kr.hhplus.be.server.presentation.controller.swagger.SwaggerQueueTokenController
import kr.hhplus.be.server.application.RequestQueueTokenUseCase
import kr.hhplus.be.server.application.GetQueueTokenUseCase
import kr.hhplus.be.server.presentation.CurrentUser
import kr.hhplus.be.server.support.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/queue/token")
class QueueTokenController(
	private val requestQueueTokenUseCase: RequestQueueTokenUseCase,
	private val getQueueTokenUseCase: GetQueueTokenUseCase
) : SwaggerQueueTokenController {

	@PostMapping
	override fun createToken(
		@CurrentUser userId: Long
	): ResponseEntity<ApiResponse<RequestQueueTokenUseCase.Output>> {
		return ResponseEntity.status(HttpStatus.CREATED).body(
			ApiResponse(
				code = "SUCCESS",
				message = "토큰 생성 성공",
				data = requestQueueTokenUseCase.createToken(userId = userId)
			)
		)
	}

	@GetMapping("/status")
	override fun getTokenStatus(
		@RequestHeader(name = "Queue-Token", required = true) token: String
	): ResponseEntity<ApiResponse<GetQueueTokenUseCase.Output>> {
		return ResponseEntity.ok(
			ApiResponse(
				code = "SUCCESS",
				message = "토큰 상태 조회 성공",
				data = getQueueTokenUseCase.getToken(token)
			)
		)
	}
}
