package kr.hhplus.be.server.application

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import java.util.*

class GetQueueTokenUseCaseTest(
	private val getQueueTokenUseCase: GetQueueTokenUseCase,
	private val queueTokenRepository: QueueTokenRepository
) : KotestIntegrationSpec({

	beforeEach {
		queueTokenRepository.clear()
	}

	given("토큰 조회 요청이 들어올 때") {

		`when`("대기(Waiting) 상태의 토큰이 존재하면") {
			then("status=WAITING, position=1, expiresAt이 반환된다") {
				// 준비: Waiting 토큰 저장
				val userId = 1L
				val waiting = QueueToken.create(userId = userId)
				val saved = queueTokenRepository.save(waiting)

				// 실행
				val output = getQueueTokenUseCase.getToken(saved.token)

				// 검증
				output.status shouldBe QueueToken.Status.WAITING.name
				output.position shouldBe 1
				output.expiresAt shouldBe saved.expiresAt
			}
		}

		`when`("활성(Active) 상태의 토큰이 존재하면") {
			then("status=ACTIVE, position=1, expiresAt이 반환된다") {
				// 준비: Active 토큰 생성/저장
				val userId = 2L
				val waiting = QueueToken.create(userId = userId)
				waiting.activate(position = 1)
				val saved = queueTokenRepository.save(waiting)

				// 실행
				val output = getQueueTokenUseCase.getToken(saved.token)

				// 검증
				output.status shouldBe QueueToken.Status.ACTIVE.name
				output.position shouldBe 1
				output.expiresAt shouldBe saved.expiresAt
			}
		}

		`when`("존재하지 않는 토큰으로 조회하면") {
			then("예외가 발생한다") {
				val missing = UUID.randomUUID().toString()
				shouldThrowExactly<IllegalArgumentException> {
					getQueueTokenUseCase.getToken(missing)
				}
			}
		}
	}
})
