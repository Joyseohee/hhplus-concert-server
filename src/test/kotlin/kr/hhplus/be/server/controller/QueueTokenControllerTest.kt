package kr.hhplus.be.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.GetQueueTokenUseCase
import kr.hhplus.be.server.application.RequestQueueTokenUseCase
import kr.hhplus.be.server.application.validation.ValidateQueueTokenService
import kr.hhplus.be.server.application.validation.ValidateUserService
import kr.hhplus.be.server.config.WebConfig
import kr.hhplus.be.server.presentation.CurrentUserResolver
import kr.hhplus.be.server.presentation.ValidateInterceptor
import kr.hhplus.be.server.presentation.controller.QueueTokenController
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
import java.time.Instant

@TestConfiguration
class QueueTokenMockConfig {
    @Bean fun requestQueueTokenUseCase() = mockk<RequestQueueTokenUseCase>(relaxed = true)
    @Bean fun getQueueTokenUseCase() = mockk<GetQueueTokenUseCase>(relaxed = true)
    @Bean fun validateQueueTokenService() = mockk<ValidateQueueTokenService>(relaxed = true)
    @Bean fun validateUserService() = mockk<ValidateUserService>(relaxed = true)
}

@WebMvcTest(QueueTokenController::class)
@Import(	WebConfig::class,
    CurrentUserResolver::class,
    ValidateInterceptor::class,
    GlobalExceptionHandler::class,
    QueueTokenMockConfig::class
)
class QueueTokenControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,

    private val requestQueueTokenUseCase: RequestQueueTokenUseCase,
    private val getQueueTokenUseCase: GetQueueTokenUseCase,
    private val validateQueueTokenService: ValidateQueueTokenService,
    private val validateUserService: ValidateUserService
) : BehaviorSpec({
    extension(SpringExtension)

    val validUserId = 1L
    val invalidUserId = 2L
    val validToken = "valid-token"

    beforeTest {
        every { requestQueueTokenUseCase.createToken(validUserId) } returns RequestQueueTokenUseCase.Output(
            token = "valid-token",
            status = "WAITING",
            position = 1,
            expiresAt = Instant.now().plusSeconds(3600)
        )
        every { getQueueTokenUseCase.getToken(validToken) } returns GetQueueTokenUseCase.Output(
            status = "WAITING",
            position = 1,
            expiresAt = Instant.now().plusSeconds(3600)
        )

        every { validateQueueTokenService.validateToken(validToken) } returns 1L
        every { validateUserService.validateUser(validUserId) } returns Unit
    }

    given("POST /api/v1/queue/token/ - 토큰 생성") {
        `when`("헤더에 유저 정보를 넣어 요청하면") {
            then("201, 토큰 정보 반환") {
                val result = mockMvc.post("/api/v1/queue/token") {
                    header("User-Id", validUserId)
                    contentType = MediaType.APPLICATION_JSON
                }.andReturn().response
                result.status shouldBe 201
                val json = result.contentAsString
                json.contains("token") shouldBe true
                json.contains("position") shouldBe true
                json.contains("expiresAt") shouldBe true
            }
        }
        `when`("헤더에 유저정보를 넣지 않고 요청하면") {
            then("500, 에러 반환") {
                val result = mockMvc.post("/api/v1/queue/token") {
                    contentType = MediaType.APPLICATION_JSON
                }.andReturn().response
                result.status shouldBe 500
            }
        }
    }

    given("GET /api/v1/queue/token/status - 토큰 상태 조회") {
        `when`("유효한 토큰으로 요청하면") {
            then("200, 토큰 상태 정보 반환") {
                val result = mockMvc.get("/api/v1/queue/token/status") {
                    header("Queue-Token", validToken)
                }.andReturn().response
                result.status shouldBe 200
                val json = objectMapper.readTree(result.contentAsString)
                json["data"]["status"].asText() shouldNotBe null
                json["data"].has("position") shouldNotBe null
                json["data"].has("expiresAt") shouldNotBe null
            }
        }
        `when`("토큰 없이 요청하면") {
            then("400, 에러 반환") {
                val result = mockMvc.get("/api/v1/queue/token/status") {
                }.andReturn().response
                result.status shouldBe 500
            }
        }
    }
})
