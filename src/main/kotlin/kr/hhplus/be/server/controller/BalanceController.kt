package kr.hhplus.be.server.controller

import kr.hhplus.be.server.controller.swagger.SwaggerBalanceController
import kr.hhplus.be.server.service.ChargeBalanceService
import kr.hhplus.be.server.service.GetBalanceService
import kr.hhplus.be.server.support.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/v1/balance")
class BalanceController(
	private val getBalanceService: GetBalanceService,
	private val chargeBalanceService: ChargeBalanceService
) : SwaggerBalanceController {
	@GetMapping("/")
	override fun getBalance(
		@RequestHeader(value = "X-Client-Id", required = true) userId: Long,
	): ResponseEntity<ApiResponse<GetBalanceService.Output>>{
		val balance = getBalanceService.getBalance(GetBalanceService.Input(userId))
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
		@RequestHeader(value = "X-Client-Id", required = true) userId: Long,
		@RequestBody balanceRequest: ChargeBalanceService.Input
	): ResponseEntity<ApiResponse<ChargeBalanceService.Output>> {
		val balance = chargeBalanceService.chargeBalance(userId, balanceRequest)
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
