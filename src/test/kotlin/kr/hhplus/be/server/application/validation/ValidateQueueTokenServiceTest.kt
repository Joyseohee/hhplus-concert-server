package kr.hhplus.be.server.application.validation

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import kr.hhplus.be.server.support.error.TokenNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.util.*

@SpringBootTest
class ValidateQueueTokenServiceTest @Autowired constructor(
    val validateQueueTokenService: ValidateQueueTokenService,
    val userBalanceRepository: UserBalanceRepository,
    val queueTokenRepository: QueueTokenRepository,
) : BehaviorSpec({
    val validUserId = 1L
    val validToken = UUID.randomUUID().toString()
    val expiredToken = UUID.randomUUID().toString()
    val notExistToken = UUID.randomUUID().toString()

    beforeTest {
        userBalanceRepository.clear()
        queueTokenRepository.clear()
        userBalanceRepository.save(
            UserBalance.create(userId = validUserId, balance = 50_000L)
        )
        queueTokenRepository.save(
            QueueToken.create(
                userId = validUserId,
                token = validToken,
                expiresAt = Instant.now().plusSeconds(60),
                status = QueueToken.Status.WAITING
            )
        )
        queueTokenRepository.save(
            QueueToken.create(
                userId = validUserId,
                token = expiredToken,
                expiresAt = Instant.now().minusSeconds(60),
                status = QueueToken.Status.WAITING
            )
        )
    }

    given("유효한 토큰이 주어졌을 때") {
        `when`("validateToken을 호출하면") {
            then("정상적으로 QueueToken 객체가 반환된다") {
                val result = validateQueueTokenService.validateToken(validToken)
                result shouldBe validUserId
            }
        }
    }

    given("존재하지 않는 토큰이 주어졌을 때") {
        `when`("validateToken을 호출하면") {
            then("예외가 발생한다") {
                shouldThrowExactly<TokenNotFoundException> {
                    validateQueueTokenService.validateToken(notExistToken)
                }
            }
        }
    }

    given("만료된 토큰이 주어졌을 때") {
        `when`("validateToken을 호출하면") {
            then("예외가 발생한다") {
                shouldThrowExactly<TokenNotFoundException> {
                    validateQueueTokenService.validateToken(expiredToken)
                }
            }
        }
    }

})
