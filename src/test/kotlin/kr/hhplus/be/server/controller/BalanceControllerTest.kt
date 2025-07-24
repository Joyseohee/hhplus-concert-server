package kr.hhplus.be.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.service.ChargeBalanceService
import kr.hhplus.be.server.service.GetBalanceService
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
	@Bean fun chargeBalanceService() = mockk<ChargeBalanceService>(relaxed = true)
	@Bean fun getBalanceService() = mockk<GetBalanceService>(relaxed = true)
}

@WebMvcTest(BalanceController::class)
@Import(GlobalExceptionHandler::class, BalanceMockConfig::class)
class BalanceControllerTest @Autowired constructor(
	private val mockMvc: MockMvc,
	private val objectMapper: ObjectMapper,
	private val chargeBalanceService: ChargeBalanceService,
	private val getBalanceService: GetBalanceService
) : BehaviorSpec({

	extension(SpringExtension)

	given("잔액 충전 API endpoint") {
		`when`("유효한 ChargeBalanceService.Input이 전송되면") {
			then("SUCCESS 응답을 반환한다") {
				val req = ChargeBalanceService.Input(amount = 10000)
				every { chargeBalanceService.chargeBalance(userId = 1L, input = req) } returns ChargeBalanceService.Output(balance = 100000)

				mockMvc.post("/api/v1/balance/charge") {
					header("X-Client-Id", "1")
					contentType = MediaType.APPLICATION_JSON
					content = objectMapper.writeValueAsString(req)
				}.andExpect {
					status { isOk() }
					jsonPath("$.code") { value("SUCCESS") }
				}
			}
		}
		`when`("유효하지 않은 ChargeBalanceService.Input이 전송되면") {
			then("BAD_REQUEST 응답을 반환한다") {
				mockMvc.post("/api/v1/balance/charge") {
					contentType = MediaType.APPLICATION_JSON
					content = "{}"
				}.andExpect {
					status { isBadRequest() }
				}
			}
		}
	}

	given("잔액 조회 API endpoint") {
		`when`("요청이 들어오면") {
			then("SUCCESS 응답을 반환한다") {
				every { getBalanceService.getBalance(GetBalanceService.Input(userId = 1L)) } returns GetBalanceService.Output(balance = 50000)

				mockMvc.get("/api/v1/balance/") {
					header("X-Client-Id", "1")
					accept = MediaType.APPLICATION_JSON
				}.andExpect {
					status { isOk() }
					jsonPath("$.code") { value("SUCCESS") }
				}
			}
		}
	}
})
