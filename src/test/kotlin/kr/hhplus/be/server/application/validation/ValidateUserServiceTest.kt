package kr.hhplus.be.server.application.validation

import io.kotest.assertions.throwables.shouldThrowExactly
import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.UserBalanceRepository


class ValidateUserServiceTest(
	private val validateUserService: ValidateUserService,
	private val userBalanceRepository: UserBalanceRepository
) : KotestIntegrationSpec({
	val INITIAL_BALANCE = 20_000L
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
})
