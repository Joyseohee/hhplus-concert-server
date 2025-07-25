package kr.hhplus.be.server.controller.swagger

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.service.ChargeBalanceService
import kr.hhplus.be.server.service.GetBalanceService
import kr.hhplus.be.server.support.ApiResponse
import org.springframework.http.ResponseEntity
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

interface SwaggerBalanceController {

	@Operation(summary = "잔액 조회", tags = ["Balance"])
	@SwaggerApiResponse(responseCode = "200", description = "조회 성공",
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
	)
	@Parameter(
		name        = "X-Client-Id",
		`in`        = ParameterIn.HEADER,
		description = "클라이언트 식별자",
		required    = true,
		schema      = Schema(
			type    = "number",
		)
	)
	fun getBalance(
		userId: Long
	): ResponseEntity<ApiResponse<GetBalanceService.Output>>

	@Operation(summary = "잔액 충전", tags = ["Balance"])
	@SwaggerApiResponse(responseCode = "200", description = "충전 성공",
		content = [ Content(mediaType = "application/json",
			schema = Schema(implementation = ApiResponse::class),
			examples = [ ExampleObject(
				"""
		  {
			"code": "SUCCESS",
			"message": "충전 성공",
			"data": { "balance": 100000 }
		  }
		  """
			) ]
		) ]
	)
	@Parameter(
		name        = "X-Client-Id",
		`in`        = ParameterIn.HEADER,
		description = "클라이언트 식별자",
		required    = true,
		schema      = Schema(
			type    = "number",
		)
	)
	fun chargeBalance(
		userId: Long,
		balanceRequest: ChargeBalanceService.Input
	): ResponseEntity<ApiResponse<ChargeBalanceService.Output>>

}
