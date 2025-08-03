package kr.hhplus.be.server.application

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ChargeBalanceUseCaseTest(
	private val chargeBalanceUseCase: ChargeBalanceUseCase,
	private val userBalanceRepository: UserBalanceRepository
) : BehaviorSpec() {
	override fun extensions() = listOf(SpringExtension)

	companion object {
		private const val INITIAL_BALANCE = 10_000L
		private const val CHARGE_AMOUNT = 5_000L
	}

	init {
		afterEach {
			userBalanceRepository.clear()
		}

		given("유효한 사용자 ID가 주어졌을 때") {
			`when`("충전 요청을 하면") {
				then("잔액이 정상적으로 증가되어 반환된다") {
					// 준비: 사용자 저장
					val user = userBalanceRepository.save(
						UserBalance.create(balance = INITIAL_BALANCE)
					)
					val input = ChargeBalanceUseCase.Input(amount = CHARGE_AMOUNT)

					// 실행
					val output = chargeBalanceUseCase.chargeBalance(user.userId!!, input)

					// 검증: 반환된 balance
					output.balance shouldBe INITIAL_BALANCE + CHARGE_AMOUNT
					// 검증: DB에 저장된 값
					val persisted = userBalanceRepository.findById(user.userId!!)
					persisted?.balance shouldBe INITIAL_BALANCE + CHARGE_AMOUNT
				}
			}
		}

		given("존재하지 않는 사용자 ID가 주어졌을 때") {
			`when`("충전 요청을 하면") {
				then("예외가 발생한다") {
					val badUserId = 999L
					val input = ChargeBalanceUseCase.Input(amount = CHARGE_AMOUNT)

					shouldThrowExactly<IllegalArgumentException> {
						chargeBalanceUseCase.chargeBalance(badUserId, input)
					}.message shouldBe "사용자가 존재하지 않습니다. 사용자 ID: $badUserId"
				}
			}
		}

		given("충전 금액이 0 이하일 때") {
			`when`("충전 요청을 하면") {
				then("예외가 발생한다") {
					val user = userBalanceRepository.save(
						UserBalance.create(balance = INITIAL_BALANCE)
					)
					val inputZero = ChargeBalanceUseCase.Input(amount = 0L)
					val inputNegative = ChargeBalanceUseCase.Input(amount = -100L)

					shouldThrowExactly<IllegalArgumentException> {
						chargeBalanceUseCase.chargeBalance(user.userId!!, inputZero)
					}
					shouldThrowExactly<IllegalArgumentException> {
						chargeBalanceUseCase.chargeBalance(user.userId!!, inputNegative)
					}
				}
			}
		}
	}
}
