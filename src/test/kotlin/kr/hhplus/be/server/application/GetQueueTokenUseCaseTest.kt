package kr.hhplus.be.server.application

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.util.*

@SpringBootTest
class GetQueueTokenUseCaseTest @Autowired constructor(
    val getQueueTokenUseCase: GetQueueTokenUseCase,
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
            UserBalance.Companion.create(userId = validUserId, balance = 50_000L)
        )
        queueTokenRepository.save(
            QueueToken.Companion.create(
                userId = validUserId,
                token = validToken,
                expiresAt = Instant.now().plusSeconds(60),
                status = QueueToken.Status.WAITING
            )
        )
        queueTokenRepository.save(
            QueueToken.Companion.create(
                userId = validUserId,
                token = expiredToken,
                expiresAt = Instant.now().minusSeconds(60),
                status = QueueToken.Status.WAITING
            )
        )
    }

    given("유효한 토큰이 주어졌을 때") {
        `when`("getToken을 호출하면") {
            then("정상적으로 Output이 반환된다") {
                val result = getQueueTokenUseCase.getToken(validToken)
                result.status shouldBe QueueToken.Status.WAITING.name
                result.position shouldBe 1
                result.expiresAt.isAfter(Instant.now()) shouldBe true
            }
        }
    }

    given("존재하지 않는 토큰이 주어졌을 때") {
        `when`("getToken을 호출하면") {
            then("예외가 발생한다") {
	            shouldThrowExactly<IllegalArgumentException> {
		            getQueueTokenUseCase.getToken(notExistToken)
	            }
            }
        }
    }

    given("만료된 토큰이 주어졌을 때") {
        `when`("getToken을 호출하면") {
            then("예외가 발생한다") {
	            shouldThrowExactly<IllegalArgumentException> {
		            getQueueTokenUseCase.getToken(expiredToken)
	            }
            }
        }
    }
})