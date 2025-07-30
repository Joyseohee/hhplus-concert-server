package kr.hhplus.be.server.application

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.model.QueueToken.Companion.MAX_ACTIVE_COUNT
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest
class RequestQueueTokenUseCaseTest @Autowired constructor(
	val requestQueueTokenUseCase: RequestQueueTokenUseCase,
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
				val result = requestQueueTokenUseCase.createToken(validUserId)
				result.position shouldBe 0
				result.token.isNotBlank() shouldBe true
				result.expiresAt.isAfter(Instant.now()) shouldBe true
			}
		}
	}

	given("토큰 저장 후 position이 정상적으로 반환되는지 확인") {
		`when`("여러 유저가 토큰을 발급받은 후") {
			then("position이 1 이상으로 반환된다") {				// 여러 유저의 토큰을 미리 저장
				val (from, to) = (2L to MAX_ACTIVE_COUNT.toLong() + 2L)
				for (i in from..to) {
					userBalanceRepository.save(UserBalance.create(userId = i, balance = 10_000L))
					requestQueueTokenUseCase.createToken(userId = i)
				}
				val result = requestQueueTokenUseCase.createToken(validUserId)
				result.position shouldBe 2
			}
		}
	}
})

