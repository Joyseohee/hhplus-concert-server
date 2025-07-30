package kr.hhplus.be.server.presentation.controller

import kr.hhplus.be.server.application.ChargeBalanceUseCase
import kr.hhplus.be.server.application.GetBalanceUseCase
import kr.hhplus.be.server.presentation.CurrentUser
import kr.hhplus.be.server.presentation.controller.swagger.SwaggerBalanceController
import kr.hhplus.be.server.support.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/balance")
class BalanceController(
	private val getBalanceUseCase: GetBalanceUseCase,
	private val chargeBalanceUseCase: ChargeBalanceUseCase
) : SwaggerBalanceController {
	@GetMapping
	override fun getBalance(
		@CurrentUser userId: Long,
	): ResponseEntity<ApiResponse<GetBalanceUseCase.Output>>{
		val balance = getBalanceUseCase.getBalance(GetBalanceUseCase.Input(userId))
		// 비즈니스 로직 구현
		return ResponseEntity.ok(
			ApiResponse(
				code = "SUCCESS",
				message = "잔액 조회 성공",
				data = balance
			)
		)
	}

	@PostMapping("/charge")
	override fun chargeBalance(
		@CurrentUser userId: Long,
		@RequestBody balanceRequest: ChargeBalanceUseCase.Input
	): ResponseEntity<ApiResponse<ChargeBalanceUseCase.Output>> {
		val balance = chargeBalanceUseCase.chargeBalance(userId, balanceRequest)
		// 비즈니스 로직 구현
		return ResponseEntity.ok(
			ApiResponse(
				code = "SUCCESS",
				message = "충전 성공",
				data = balance
			)
		)
	}
}
