package kr.hhplus.be.server.service.validation

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.QueueToken
import kr.hhplus.be.server.domain.QueueToken.Companion.MAX_ACTIVE_COUNT
import kr.hhplus.be.server.domain.QueueTokenRepository
import kr.hhplus.be.server.domain.UserBalance
import kr.hhplus.be.server.domain.UserBalanceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.util.UUID

@SpringBootTest
class ValidateQueueTokenServiceTest @Autowired constructor(
    val validateQueueTokenService: ValidateQueueTokenService,
    val userBalanceRepository: UserBalanceRepository,
    val queueTokenRepository: QueueTokenRepository,
) : BehaviorSpec({
    val validUserId = 1L
    val invalidUserId = 2L
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
                result.token shouldBe validToken
                result.userId shouldBe validUserId
            }
        }
    }

    given("존재하지 않는 토큰이 주어졌을 때") {
        `when`("validateToken을 호출하면") {
            then("예외가 발생한다") {
                shouldThrowExactly<IllegalArgumentException> {
                    validateQueueTokenService.validateToken(notExistToken)
                }.message shouldBe "토큰을 찾을 수 없습니다."
            }
        }
    }

    given("존재하지 않는 유저가 주어졌을 때") {
        val token = UUID.randomUUID().toString()
        beforeTest {
            queueTokenRepository.save(
                QueueToken.create(
                    userId = invalidUserId,
                    token = token,
                    expiresAt = Instant.now().plusSeconds(60),
                    status = QueueToken.Status.WAITING
                )
            )
        }
        `when`("validateToken을 호출하면") {
            then("예외가 발생한다") {
                shouldThrowExactly<IllegalArgumentException> {
                    validateQueueTokenService.validateToken(token)
                }.message shouldBe "사용자를 찾을 수 없습니다."
            }
        }
    }

    given("만료된 토큰이 주어졌을 때") {
        `when`("validateToken을 호출하면") {
            then("예외가 발생한다") {
                shouldThrowExactly<IllegalArgumentException> {
                    validateQueueTokenService.validateToken(expiredToken)
                }.message?.contains("토큰은") shouldBe true
            }
        }
    }

    given("토큰으로 요청을 보냈는데") {
        `when`("대기 중인 토큰이 ACTIVE 슬롯 여유가 있을 때") {
            then("WAITING 상태의 토큰이 ACTIVE 상태로 전이된다") {
                // 1) 사용자 준비
                val user = UserBalance.create(
                    userId   = 1L,
                    balance  = 1_000L
                )
                userBalanceRepository.save(user)

                // 2) ACTIVE 상태 토큰 10개 저장 (50명 미만)
                repeat(10) {
                    val t = QueueToken.create(userId = user.userId!!).apply {
                        activate(activeCount = it)
                    }
                    t.activate(activeCount = 0)  // 강제로 ACTIVE
                    queueTokenRepository.save(t)
                }

                // 3) WAITING 상태 토큰 저장
                val waiting = QueueToken.create(
                    userId = user.userId!!,
                    expiresAt = Instant.now().plusSeconds(60 * 5)
                )
                queueTokenRepository.save(waiting)

                // 4) 서비스 호출
                val result = validateQueueTokenService.validateToken(waiting.token)

                // 5) 반환된 토큰 상태 검증
                result.status shouldBe QueueToken.Status.ACTIVE

                // 6) 영속화된 토큰 상태도 ACTIVE인지 확인
                val persisted = queueTokenRepository.findByToken(waiting.token)!!
                persisted.status shouldBe QueueToken.Status.ACTIVE
            }
        }
    }
})
