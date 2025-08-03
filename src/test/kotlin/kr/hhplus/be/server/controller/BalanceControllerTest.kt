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
import kr.hhplus.be.server.presentation.controller.BalanceController
import kr.hhplus.be.server.support.error.GlobalExceptionHandler
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(BalanceController::class)
@Import(
	WebConfig::class,
	GlobalExceptionHandler::class,
	BalanceControllerTest.BalanceMockConfig::class
)
@ActiveProfiles("test")
class BalanceControllerTest(
	private val mockMvc: MockMvc,
	private val objectMapper: ObjectMapper,
	private val chargeBalanceUseCase: ChargeBalanceUseCase,
	private val getBalanceUseCase: GetBalanceUseCase,
	private val validateUserService: ValidateUserService
) : BehaviorSpec() {

	override fun extensions() = listOf(SpringExtension)

	companion object {
		private const val USER_ID_HEADER = "User-Id"
		private val CHARGE_REQ = ChargeBalanceUseCase.Input(amount = 10_000)
		private val BALANCE_REQ = GetBalanceUseCase.Input(userId = 1L)
	}

	init {
		beforeEach {
			every { validateUserService.validateUser(1L) } returns Unit
		}

		given("잔액 충전 API POST /api/v1/balance/charge") {
			`when`("유효한 요청이 오면") {
				then("200 OK, SUCCESS 코드를 반환한다") {
					every { chargeBalanceUseCase.chargeBalance(1L, CHARGE_REQ) }
						.returns(ChargeBalanceUseCase.Output(balance = 100_000))

					mockMvc.post("/api/v1/balance/charge") {
						header(USER_ID_HEADER, "1")
						contentType = MediaType.APPLICATION_JSON
						content = objectMapper.writeValueAsString(CHARGE_REQ)
					}.andExpect {
						status { isOk() }
						jsonPath("$.code") { value("SUCCESS") }
					}
				}
			}

			`when`("잘못된 요청이 오면") {
				then("500 Internal Server Error를 반환한다") {
					mockMvc.post("/api/v1/balance/charge") {
						contentType = MediaType.APPLICATION_JSON
						content = "{}"
					}.andExpect {
						status { isInternalServerError() }
					}
				}
			}
		}

		given("잔액 조회 API GET /api/v1/balance") {
			`when`("요청이 오면") {
				then("200 OK, SUCCESS 코드를 반환한다") {
					every { getBalanceUseCase.getBalance(BALANCE_REQ) }
						.returns(GetBalanceUseCase.Output(balance = 50_000))

					mockMvc.get("/api/v1/balance") {
						header(USER_ID_HEADER, "1")
						accept = MediaType.APPLICATION_JSON
					}.andExpect {
						status { isOk() }
						jsonPath("$.code") { value("SUCCESS") }
					}
				}
			}
		}
	}

	@TestConfiguration
	class BalanceMockConfig {
		@Bean
		fun chargeBalanceUseCase() = mockk<ChargeBalanceUseCase>(relaxed = true)

		@Bean
		fun getBalanceUseCase() = mockk<GetBalanceUseCase>(relaxed = true)

		@Bean
		fun validateQueueTokenService() = mockk<ValidateQueueTokenService>(relaxed = true)

		@Bean
		fun validateUserService() = mockk<ValidateUserService>(relaxed = true)
	}
}
