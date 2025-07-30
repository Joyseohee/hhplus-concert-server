package kr.hhplus.be.server.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.UserBalance
import kr.hhplus.be.server.domain.UserBalanceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GetBalanceServiceTest @Autowired constructor(
    private val getBalanceService: GetBalanceService,
    private val balanceRepository: UserBalanceRepository
) : BehaviorSpec({

    val userId = 1L
    val expectedBalance = 30_000L

    beforeTest {
        balanceRepository.clear()
        balanceRepository.save(UserBalance.create(userId, expectedBalance)) // 초기 데이터 설정
    }

    given("사용자의 잔액을 조회할 때") {
        `when`("유효한 사용자의 잔액을 조회하면") {
            then("잔액이 올바르게 반환되어야 한다") {
                val input = GetBalanceService.Input(userId)
                val output = getBalanceService.getBalance(input)

                output.balance shouldBe expectedBalance
            }
        }

        `when`("존재하지 않는 사용자의 잔액을 조회할 때") {
            val invalidUserId = 999L

            then("예외가 발생해야 한다") {
                val input = GetBalanceService.Input(invalidUserId)
                shouldThrow<IllegalArgumentException> {
                    getBalanceService.getBalance(input)
                }
            }
        }
    }
})
