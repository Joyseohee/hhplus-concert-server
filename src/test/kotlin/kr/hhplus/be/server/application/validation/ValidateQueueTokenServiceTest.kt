package kr.hhplus.be.server.application.validation

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import java.time.Instant
import java.util.*

class ValidateQueueTokenServiceTest(
    private val validateQueueTokenService: ValidateQueueTokenService,
    private val userBalanceRepository: UserBalanceRepository,
    private val queueTokenRepository: QueueTokenRepository
) : KotestIntegrationSpec({

    val INITIAL_BALANCE = 50_000L

    beforeEach {
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
                val savedToken = queueTokenRepository.save(
                    QueueToken.create(
                        userId = user.userId!!,
                        token = validToken,
                        expiresAt = Instant.now().plusSeconds(60),
                        status = QueueToken.Status.ACTIVE
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
                val savedToken = queueTokenRepository.save(
                    QueueToken.create(
                        userId = user.userId!!,
                        token = expiredToken,
                        expiresAt = Instant.now().minusSeconds(60),
                        status = QueueToken.Status.ACTIVE
                    )
                )

                shouldThrowExactly<IllegalArgumentException> {
                    validateQueueTokenService.validateToken(expiredToken)
                }
            }
        }
    }

    given("활성화되지 않은 토큰이 주어졌을 때") {
        `when`("validateToken을 호출하면") {
            then("예외가 발생한다") {
                val user = userBalanceRepository.save(
                    UserBalance.create(balance = INITIAL_BALANCE)
                )
                val waitingToken = UUID.randomUUID().toString()
                val savedToken = queueTokenRepository.save(
                    QueueToken.create(
                        userId = user.userId!!,
                        token = waitingToken,
                        expiresAt = Instant.now().plusSeconds(60),
                        status = QueueToken.Status.WAITING
                    )
                )

                shouldThrowExactly<IllegalArgumentException> {
                    validateQueueTokenService.validateToken(waitingToken)
                }
            }
        }
    }
})
