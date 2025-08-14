package kr.hhplus.be.server.application

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.transaction.Transactional
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class ChargeBalanceUseCase(
    private val userBalanceRepository: UserBalanceRepository
) {
    @Retryable(
        value = [ObjectOptimisticLockingFailureException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 200, multiplier = 2.0)
    )
    @Transactional
    fun chargeBalance(userId: Long, input: Input): Output {
        val userBalance = userBalanceRepository.findById(userId)
            ?: throw IllegalArgumentException("사용자가 존재하지 않습니다. 사용자 ID: $userId")

        userBalance.charge(input.amount)

        return Output(
            balance = userBalance.balance
        )
    }

    @Recover
    fun recover(e: ObjectOptimisticLockingFailureException, userId: Long, input: Input): Output {
        throw RuntimeException("포인트 차감에 실패했습니다. 나중에 다시 시도해주세요.")
    }

    @Schema(name = "ChargeBalanceRequest", description = "잔액 충전 요청")
    data class Input(
        @Schema(description = "충전액", example = "300000", requiredMode = Schema.RequiredMode.REQUIRED)
        val amount: Long
    )

    @Schema(name = "ChargeBalanceResponse", description = "잔액 충전 응답")
    data class Output(
        @Schema(description = "잔액", example = "300000")
        val balance: Long
    )
}
