package kr.hhplus.be.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.service.GetQueueTokenService
import kr.hhplus.be.server.service.RequestQueueTokenService
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
    @Bean fun requestQueueTokenService() = mockk<RequestQueueTokenService>(relaxed = true)
    @Bean fun getQueueTokenService() = mockk<GetQueueTokenService>(relaxed = true)
}

@WebMvcTest(QueueTokenController::class)
@Import(QueueTokenMockConfig::class, GlobalExceptionHandler::class)
class QueueTokenControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,

    private val requestQueueTokenService: RequestQueueTokenService,
    private val getQueueTokenService: GetQueueTokenService
) : BehaviorSpec({
    extension(SpringExtension)

    val validUserId = 1L
    val invalidUserId = 2L

    beforeTest {
        every { requestQueueTokenService.createToken(validUserId) } returns RequestQueueTokenService.Output(
            token = "valid-token",
            status = "WAITING",
            position = 1,
            expiresAt = Instant.now().plusSeconds(3600)
        )
        every { getQueueTokenService.getToken("valid-token") } returns GetQueueTokenService.Output(
            status = "WAITING",
            position = 1,
            expiresAt = Instant.now().plusSeconds(3600)
        )
    }

    given("POST /api/v1/queue/token/ - 토큰 생성") {
        `when`("헤더에 유저 정보를 넣어 요청하면") {
            then("201, 토큰 정보 반환") {
                val result = mockMvc.post("/api/v1/queue/token/") {
                    header("X-Client-Id", validUserId)
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
            then("400, 에러 반환") {
                val result = mockMvc.post("/api/v1/queue/token/") {
                    contentType = MediaType.APPLICATION_JSON
                }.andReturn().response
                result.status shouldBe 400
            }
        }
    }

    given("GET /api/v1/queue/token/ - 토큰 상태 조회") {
        var token: String = ""
        beforeTest {
            // 토큰 발급
            val result = mockMvc.post("/api/v1/queue/token/") {
                header("X-Client-Id", validUserId)
                contentType = MediaType.APPLICATION_JSON
            }.andReturn().response
            val json = objectMapper.readTree(result.contentAsString)
            token = json["data"]["token"].asText()
        }
        `when`("유효한 토큰으로 요청하면") {
            then("200, 토큰 상태 정보 반환") {
                val result = mockMvc.get("/api/v1/queue/token/") {
                    header("X-Queue-Token", token)
                }.andReturn().response
                result.status shouldBe 200
                val json = objectMapper.readTree(result.contentAsString)
                json["data"]["status"].asText() shouldBe "WAITING"
                json["data"].has("position") shouldBe true
                json["data"].has("expiresAt") shouldBe true
            }
        }
        `when`("토큰 없이 요청하면") {
            then("400, 에러 반환") {
                val result = mockMvc.get("/api/v1/queue/token/") {
                }.andReturn().response
                result.status shouldBe 400
            }
        }
    }
})
