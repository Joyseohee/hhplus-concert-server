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
class GetBalanceUseCaseTest(
    private val getBalanceUseCase: GetBalanceUseCase,
    private val userBalanceRepository: UserBalanceRepository
) : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    init {
        afterEach {
            userBalanceRepository.clear()
        }

        given("잔액 조회 요청이 들어오면") {
            `when`("존재하는 사용자 ID로 요청하면") {
                then("현재 잔액을 반환해야 한다") {
                    val saved = userBalanceRepository.save(
                        UserBalance.create(balance = 123_456L)
                    )

                    val output = getBalanceUseCase.getBalance(
                        GetBalanceUseCase.Input(userId = saved.userId!!)
                    )

                    output.balance shouldBe 123_456L
                }
            }

            `when`("존재하지 않는 사용자 ID로 요청하면") {
                then("IllegalArgumentException이 발생해야 한다") {
                    val missingUserId = 999L

                    shouldThrowExactly<IllegalArgumentException> {
                        getBalanceUseCase.getBalance(
                            GetBalanceUseCase.Input(userId = missingUserId)
                        )
                    }.message?.contains("사용자 잔액을 찾을 수 없습니다") shouldBe true
                }
            }
        }
    }
}
