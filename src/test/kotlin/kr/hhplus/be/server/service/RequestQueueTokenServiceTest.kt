package kr.hhplus.be.server.service

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.QueueTokenRepository
import kr.hhplus.be.server.domain.UserBalance
import kr.hhplus.be.server.domain.UserBalanceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest
class RequestQueueTokenServiceTest @Autowired constructor(
    val requestQueueTokenService: RequestQueueTokenService,
    val userBalanceRepository: UserBalanceRepository,
    val queueTokenRepository: QueueTokenRepository,
) : BehaviorSpec({
    val validUserId = 1L
    val invalidUserId = 2L

    beforeTest {
        userBalanceRepository.clear()
        queueTokenRepository.clear()
        userBalanceRepository.save(
            UserBalance.create(userId = validUserId, balance = 50_000L)
        )
    }

    given("유효한 유저가 토큰을 요청할 때") {
        `when`("createToken을 호출하면") {
            then("정상적으로 Output이 반환된다") {
                val result = requestQueueTokenService.createToken(validUserId)
                result!!.status shouldBe "WAITING"
                result.position shouldBe 1
                result.token.isNotBlank() shouldBe true
                result.expiresAt.isAfter(Instant.now()) shouldBe true
            }
        }
    }

    given("존재하지 않는 유저가 토큰을 요청할 때") {
        `when`("createToken을 호출하면") {
            then("예외가 발생한다") {
                shouldThrowExactly<IllegalArgumentException> {
                    requestQueueTokenService.createToken(invalidUserId)
                }.message shouldBe "사용자를 찾을 수 없습니다."
            }
        }
    }

    given("토큰 저장 후 position이 정상적으로 반환되는지 확인") {
        beforeTest {
            // 여러 유저의 토큰을 미리 저장
            for (i in 2L..5L) {
                userBalanceRepository.save(UserBalance.create(userId = i, balance = 10_000L))
                requestQueueTokenService.createToken(i)
            }
        }
        `when`("여러 유저가 토큰을 발급받은 후") {
            then("position이 1 이상으로 반환된다") {
                val result = requestQueueTokenService.createToken(validUserId)
                result!!.position shouldBeGreaterThan 0
            }
        }
    }
})

