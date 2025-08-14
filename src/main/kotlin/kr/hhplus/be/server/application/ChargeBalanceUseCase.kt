package kr.hhplus.be.server.application

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.transaction.Transactional
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import kr.hhplus.be.server.support.annotation.RedisLock
import org.springframework.stereotype.Service

@Service
class ChargeBalanceUseCase(
    private val userBalanceRepository: UserBalanceRepository
) {

    @RedisLock(
        key = "'lock:balance:{' + #userId + '}'",
        waitTimeMs = 1000, leaseTimeMs = 2000, failFast = false
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
