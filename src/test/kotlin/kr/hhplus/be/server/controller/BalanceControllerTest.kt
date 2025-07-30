package kr.hhplus.be.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.ChargeBalanceUseCase
import kr.hhplus.be.server.application.GetBalanceUseCase
import kr.hhplus.be.server.application.validation.ValidateQueueTokenService
import kr.hhplus.be.server.application.validation.ValidateUserService
import kr.hhplus.be.server.config.WebConfig
import kr.hhplus.be.server.presentation.CurrentUserResolver
import kr.hhplus.be.server.presentation.ValidateInterceptor
import kr.hhplus.be.server.presentation.controller.BalanceController
import kr.hhplus.be.server.support.error.GlobalExceptionHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@TestConfiguration
class BalanceMockConfig {
	@Bean fun chargeBalanceUseCase() = mockk<ChargeBalanceUseCase>(relaxed = true)
	@Bean fun getBalanceUseCase() = mockk<GetBalanceUseCase>(relaxed = true)
	@Bean fun validateQueueTokenService() = mockk<ValidateQueueTokenService>(relaxed = true)
	@Bean fun validateUserService() = mockk<ValidateUserService>(relaxed = true)
}

@WebMvcTest(BalanceController::class)
@Import(
	WebConfig::class,
	CurrentUserResolver::class,
	ValidateInterceptor::class,
	GlobalExceptionHandler::class,
	BalanceMockConfig::class
)
class BalanceControllerTest @Autowired constructor(
	private val mockMvc: MockMvc,
	private val objectMapper: ObjectMapper,
	private val chargeBalanceUseCase: ChargeBalanceUseCase,
	private val getBalanceUseCase: GetBalanceUseCase,
	private val validateQueueTokenService: ValidateQueueTokenService,
	private val validateUserService: ValidateUserService
) : BehaviorSpec({

	extension(SpringExtension)

	val validToken = "valid-token"
	every { validateQueueTokenService.validateToken(validToken) } returns 1L
	every { validateUserService.validateUser(1L) } returns Unit

	given("잔액 충전 API endpoint") {
		`when`("유효한 ChargeBalanceUseCase.Input이 전송되면") {
			then("SUCCESS 응답을 반환한다") {
				val req = ChargeBalanceUseCase.Input(amount = 10000)
				every { chargeBalanceUseCase.chargeBalance(userId = 1L, input = req) } returns ChargeBalanceUseCase.Output(balance = 100000)

				mockMvc.post("/api/v1/balance/charge") {
					header("User-Id", "1")
					contentType = MediaType.APPLICATION_JSON
					content = objectMapper.writeValueAsString(req)
				}.andExpect {
					status { isOk() }
					jsonPath("$.code") { value("SUCCESS") }
				}
			}
		}
		`when`("유효하지 않은 ChargeBalanceUseCase.Input이 전송되면") {
			then("BAD_REQUEST 응답을 반환한다") {
				mockMvc.post("/api/v1/balance/charge") {
					contentType = MediaType.APPLICATION_JSON
					content = "{}"
				}.andExpect {
					status { isInternalServerError() }
				}
			}
		}
	}

	given("잔액 조회 API endpoint") {
		`when`("요청이 들어오면") {
			then("SUCCESS 응답을 반환한다") {
				every { getBalanceUseCase.getBalance(GetBalanceUseCase.Input(userId = 1L)) } returns GetBalanceUseCase.Output(balance = 50000)

				mockMvc.get("/api/v1/balance") {
					header("User-Id", "1")
					accept = MediaType.APPLICATION_JSON
				}.andExpect {
					status { isOk() }
					jsonPath("$.code") { value("SUCCESS") }
				}
			}
		}
	}
})
