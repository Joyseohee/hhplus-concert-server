package kr.hhplus.be.server.application

import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.model.QueueToken.Companion.MAX_ACTIVE_COUNT
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

class RequestQueueTokenUseCaseTest(
	private val requestQueueTokenUseCase: RequestQueueTokenUseCase,
	private val queueTokenRepository: QueueTokenRepository
) : KotestIntegrationSpec({

	afterEach {
		queueTokenRepository.clear()
	}

	given("새 토큰 발급을 요청할 때") {
		`when`("현재 활성 토큰 수가 49개 미만인 경우") {
			then("토큰이 ACTIVE 상태로 발급되어 저장된다") {
				repeat(10) {
					queueTokenRepository.save(
						QueueToken.create(userId = it.toLong(), status = QueueToken.Status.ACTIVE)
					)
				}
				val before = Instant.now()

				val output = requestQueueTokenUseCase.createToken(userId = 100L)

				output.status shouldBe QueueToken.Status.ACTIVE.name
				val saved = queueTokenRepository.findValidatedByToken(output.token)!!
				saved.status shouldBe QueueToken.Status.ACTIVE
				val diff = ChronoUnit.MINUTES.between(before, saved.expiresAt)
				diff shouldBe 5L
			}
		}

		`when`("현재 활성 토큰 수가 50개 이상인 경우") {
			then("토큰이 WAITING 상태로 발급되어 저장된다") {
				repeat(MAX_ACTIVE_COUNT) {
					queueTokenRepository.save(
						QueueToken.create(userId = it.toLong(), status = QueueToken.Status.ACTIVE)
					)
				}

				val output = requestQueueTokenUseCase.createToken(userId = 200L)

				output.status shouldBe QueueToken.Status.WAITING.name
				val saved = queueTokenRepository.findValidatedByToken(output.token)!!
				saved.status shouldBe QueueToken.Status.WAITING
			}
		}
	}
})
