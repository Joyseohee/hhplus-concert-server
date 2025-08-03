package kr.hhplus.be.server.application.validation

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ValidateUserServiceTest(
	private val validateUserService: ValidateUserService,
	private val userBalanceRepository: UserBalanceRepository
) : BehaviorSpec() {

	override fun extensions() = listOf(SpringExtension)

	companion object {
		private const val INITIAL_BALANCE = 20_000L
	}

	init {
		afterEach {
			userBalanceRepository.clear()
		}

		given("사용자 유효성 검증을 수행할 때") {
			`when`("존재하는 사용자 ID를 검증하면") {
				then("예외 없이 통과해야 한다") {
					val user = userBalanceRepository.save(
						UserBalance.create(balance = INITIAL_BALANCE)
					)

					validateUserService.validateUser(user.userId!!)
				}
			}

			`when`("존재하지 않는 사용자 ID를 검증하면") {
				then("IllegalArgumentException 발생해야 한다") {
					shouldThrowExactly<IllegalArgumentException> {
						validateUserService.validateUser(999L)
					}
				}
			}
		}
	}
}
