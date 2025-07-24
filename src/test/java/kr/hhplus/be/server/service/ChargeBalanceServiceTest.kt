package kr.hhplus.be.server.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.UserBalance
import kr.hhplus.be.server.domain.UserBalanceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ChargeBalanceServiceTest @Autowired constructor(
    private val service: ChargeBalanceService,
    private val userBalanceRepository: UserBalanceRepository
)  : BehaviorSpec({

    val userId = 1L
    val inValidUserId = 999L
    val initialBalance = 10_000L
    val chargeAmount = 30_000L

    beforeTest {
        userBalanceRepository.clear()
        userBalanceRepository.save(UserBalance.create(userId = userId, balance = initialBalance))
    }

    given("충전 요청이 들어왔을 때") {
        `when`("유효한 사용자 ID로 충전을 요청한다면") {
            val input = ChargeBalanceService.Input(amount = chargeAmount)
            then("잔액이 올바르게 충전되어야 한다") {
                val output = service.chargeBalance(userId, input)
                output.balance shouldBe initialBalance + chargeAmount
            }
        }

        `when`("유효하지 않은 사용자 ID로 충전을 요청할 때") {
            then("예외가 발생해야 한다") {
                val input = ChargeBalanceService.Input(amount = chargeAmount)
                shouldThrow<IllegalArgumentException> {
                    service.chargeBalance(inValidUserId, input)
                }.message shouldBe "사용자를 찾을 수 없습니다."
            }
        }
    }
})
