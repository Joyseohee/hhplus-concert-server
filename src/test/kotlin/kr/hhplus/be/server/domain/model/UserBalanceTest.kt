package kr.hhplus.be.server.domain.model

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.model.UserBalance.Companion.MAX_POINT
import kr.hhplus.be.server.domain.model.UserBalance.Companion.MIN_TRANSACTION_AMOUNT

class UserBalanceTest : FreeSpec({
    val userId = 1L
    val initialBalance = 5_000L


    "잔고 충전" - {
        "양수 금액을 충전하면 잔고가 증가한다" {
            val userBalance = UserBalance.create(userId = userId, balance = initialBalance)

            val chargeAmount = 50L
            userBalance.charge(chargeAmount)

            userBalance.balance shouldBe initialBalance + chargeAmount
        }

        "0원 이하 금액을 충전하면 예외가 발생한다" {

            listOf(0L, -10L).forEach { invalidAmount ->
                val userBalance = UserBalance.create(userId = userId, balance = initialBalance)
                val exception = shouldThrowExactly<IllegalArgumentException> {
                    userBalance.charge(invalidAmount)
                }
                exception.message shouldBe  "충전 금액은 $MIN_TRANSACTION_AMOUNT 이상이어야 합니다. 현재: $invalidAmount"
            }
        }

        "충전 시 잔고가 50만원을 초과하면 예외가 발생한다" {
            val userBalance = UserBalance.create(userId = userId, balance = initialBalance)

            val chargeAmount = 500_000L

            val exception = shouldThrowExactly<IllegalArgumentException> {
                userBalance.charge(chargeAmount)
            }

            exception.message shouldBe "잔고는 ${MAX_POINT}를 초과할 수 없습니다. 현재 잔고: $initialBalance, 충전 금액: $chargeAmount"
        }
    }

    "잔고 사용" - {

        "잔고를 초과하지 않은 금액을 사용하면 잔고를 차감한다" {
            listOf(50L, 100L).forEach { useAmount ->
                val userBalance = UserBalance.create(userId = userId, balance = initialBalance)

                userBalance.use(useAmount)
                userBalance.balance shouldBe initialBalance - useAmount
                userBalance.balance!! shouldBeGreaterThanOrEqual 0L
            }
        }

        "0원 이하의 금액을 사용하면 예외가 발생한다" {
            listOf(0L, -10L).forEach{ invalidAmount ->
                val userBalance = UserBalance.create(userId = userId, balance = initialBalance)

                val exception = shouldThrowExactly<IllegalArgumentException> {
                    userBalance.use(invalidAmount)
                }
                exception.message shouldBe  "사용 금액은 $MIN_TRANSACTION_AMOUNT 이상이어야 합니다. 사용 금액: $invalidAmount"
            }
        }

        "잔고 이상의 금액을 사용하면 예외가 발생한다" {
            listOf(10_000L, 20_000L).forEach{ useAmount ->
                val userBalance = UserBalance.create(userId = userId, balance = initialBalance)

                val exception = shouldThrowExactly<IllegalArgumentException> {
                    userBalance.use(useAmount)
                }
                exception.message shouldBe "잔고가 부족합니다. 현재 잔고 : $initialBalance , 사용 금액 : $useAmount"
            }
        }
    }
})


