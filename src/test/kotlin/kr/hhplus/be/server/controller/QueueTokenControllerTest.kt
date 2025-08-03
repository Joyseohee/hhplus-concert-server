package kr.hhplus.be.server.controller

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.GetQueueTokenUseCase
import kr.hhplus.be.server.application.RequestQueueTokenUseCase
import kr.hhplus.be.server.application.validation.ValidateQueueTokenService
import kr.hhplus.be.server.application.validation.ValidateUserService
import kr.hhplus.be.server.config.WebConfig
import kr.hhplus.be.server.presentation.controller.QueueTokenController
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
import java.time.Instant

@WebMvcTest(QueueTokenController::class)
@Import(
	WebConfig::class,
	GlobalExceptionHandler::class,
	QueueTokenControllerTest.QueueTokenMockConfig::class
)
@ActiveProfiles("test")
class QueueTokenControllerTest(
	private val mockMvc: MockMvc,
	private val requestQueueTokenUseCase: RequestQueueTokenUseCase,
	private val getQueueTokenUseCase: GetQueueTokenUseCase,
	private val validateQueueTokenService: ValidateQueueTokenService,
	private val validateUserService: ValidateUserService
) : BehaviorSpec() {

	override fun extensions() = listOf(SpringExtension)

	companion object {
		private const val USER_ID_HEADER = "User-Id"
		private const val QUEUE_TOKEN_HEADER = "Queue-Token"
		private const val VALID_USER_ID = 1L
		private const val VALID_TOKEN = "valid-token"
		private val EXPIRES_AT = Instant.now().plusSeconds(3600)
	}

	init {
		beforeEach {
			every { validateUserService.validateUser(VALID_USER_ID) } returns Unit
			every { requestQueueTokenUseCase.createToken(VALID_USER_ID) } returns
					RequestQueueTokenUseCase.Output(
						token = VALID_TOKEN,
						status = "WAITING",
						position = 1,
						expiresAt = EXPIRES_AT
					)
			every { validateQueueTokenService.validateToken(VALID_TOKEN) } returns VALID_USER_ID
			every { getQueueTokenUseCase.getToken(VALID_TOKEN) } returns
					GetQueueTokenUseCase.Output(
						status = "WAITING",
						position = 1,
						expiresAt = EXPIRES_AT
					)
		}

		given("대기열 토큰 발급 요청 POST /api/v1/queue/token") {
			`when`("User-Id 헤더를 포함해 요청하면") {
				then("201 Created 및 토큰 정보 반환") {
					mockMvc.post("/api/v1/queue/token") {
						header(USER_ID_HEADER, VALID_USER_ID)
						accept = MediaType.APPLICATION_JSON
					}.andExpect {
						status { isCreated() }
						jsonPath("$.data.token") { value(VALID_TOKEN) }
						jsonPath("$.data.position") { value(1) }
						jsonPath("$.data.expiresAt") { exists() }
					}
				}
			}
			`when`("헤더가 없으면") {
				then("500 Internal Server Error") {
					mockMvc.post("/api/v1/queue/token") {
						accept = MediaType.APPLICATION_JSON
					}.andExpect {
						status { isInternalServerError() }
					}
				}
			}
		}

		given("대기열 토큰 조회 요청 GET /api/v1/queue/token/status") {
			`when`("Queue-Token 헤더를 포함해 요청하면") {
				then("200 OK 및 상태 정보 반환") {
					mockMvc.get("/api/v1/queue/token/status") {
						header(QUEUE_TOKEN_HEADER, VALID_TOKEN)
						accept = MediaType.APPLICATION_JSON
					}.andExpect {
						status { isOk() }
						jsonPath("$.data.status") { value("WAITING") }
						jsonPath("$.data.position") { value(1) }
						jsonPath("$.data.expiresAt") { exists() }
					}
				}
			}
			`when`("헤더가 없으면") {
				then("500 Internal Server Error") {
					mockMvc.get("/api/v1/queue/token/status") {
						accept = MediaType.APPLICATION_JSON
					}.andExpect {
						status { isInternalServerError() }
					}
				}
			}
		}
	}

	@TestConfiguration
	class QueueTokenMockConfig {
		@Bean
		fun requestQueueTokenUseCase() = mockk<RequestQueueTokenUseCase>(relaxed = true)
		@Bean
		fun getQueueTokenUseCase() = mockk<GetQueueTokenUseCase>(relaxed = true)
		@Bean
		fun validateQueueTokenService() = mockk<ValidateQueueTokenService>(relaxed = true)
		@Bean
		fun validateUserService() = mockk<ValidateUserService>(relaxed = true)
	}
}
