package kr.hhplus.be.server.application.validation

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
class ValidateQueueTokenServiceTest(
    private val validateQueueTokenService: ValidateQueueTokenService,
    private val userBalanceRepository: UserBalanceRepository,
    private val queueTokenRepository: QueueTokenRepository
) : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    companion object {
        private const val INITIAL_BALANCE = 50_000L
    }

    init {
        afterEach {
            userBalanceRepository.clear()
            queueTokenRepository.clear()
        }

        given("유효한 토큰이 주어졌을 때") {
            `when`("validateToken을 호출하면") {
                then("정상적으로 사용자 ID가 반환된다") {
                    val user = userBalanceRepository.save(
                        UserBalance.create(balance = INITIAL_BALANCE)
                    )
                    val validToken = UUID.randomUUID().toString()
                    queueTokenRepository.save(
                        QueueToken.create(
                            userId    = user.userId!!,
                            token     = validToken,
                            expiresAt = Instant.now().plusSeconds(60),
                            status    = QueueToken.Status.WAITING
                        )
                    )

                    val result = validateQueueTokenService.validateToken(validToken)

                    result shouldBe user.userId
                }
            }
        }

        given("존재하지 않는 토큰이 주어졌을 때") {
            `when`("validateToken을 호출하면") {
                then("예외가 발생한다") {
                    val notExistToken = UUID.randomUUID().toString()

                    shouldThrowExactly<IllegalArgumentException> {
                        validateQueueTokenService.validateToken(notExistToken)
                    }
                }
            }
        }

        given("만료된 토큰이 주어졌을 때") {
            `when`("validateToken을 호출하면") {
                then("예외가 발생한다") {
                    val user = userBalanceRepository.save(
                        UserBalance.create(balance = INITIAL_BALANCE)
                    )
                    val expiredToken = UUID.randomUUID().toString()
                    queueTokenRepository.save(
                        QueueToken.create(
                            userId    = user.userId!!,
                            token     = expiredToken,
                            expiresAt = Instant.now().minusSeconds(60),
                            status    = QueueToken.Status.WAITING
                        )
                    )

                    shouldThrowExactly<IllegalArgumentException> {
                        validateQueueTokenService.validateToken(expiredToken)
                    }
                }
            }
        }
    }
}
