package kr.hhplus.be.server.application

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.application.schedule.ExpireStatusScheduler
import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.repository.QueueTokenRepository

class RequestQueueTokenUseCaseTest(
	private val requestQueueTokenUseCase: RequestQueueTokenUseCase,
	private val queueTokenRepository: QueueTokenRepository
) : KotestIntegrationSpec({

	beforeEach {
		queueTokenRepository.clear()
	}

	given("새 토큰 발급을 요청할 때") {
		`when`("현재 활성 토큰 수와 무관하게") {
			then("토큰이 WAITING 상태로 발급되어 저장된다") {
				val output = requestQueueTokenUseCase.createToken(userId = 100L)

				output.status shouldBe QueueToken.Status.WAITING.name
				val saved = queueTokenRepository.findByToken(output.token)!!
				saved.status shouldBe QueueToken.Status.WAITING
			}
		}
	}

})
