package kr.hhplus.be.server.service

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.UserBalanceRepository
import org.springframework.stereotype.Service

@Service
class GetBalanceService(
    private val balanceRepository: UserBalanceRepository
)
{
    fun getBalance(input: Input): Output {
        val balance = balanceRepository.findById(input.userId)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: userId=${input.userId}")

        return Output(balance = balance.balance)
    }

    @Schema(name = "GetBalanceRequest", description = "잔액 조회 요청")
    data class Input(
        @Schema(description = "사용자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        val userId: Long
    )

    @Schema(name = "GetBalanceResponse", description = "잔액 조회 응답")
    data class Output(
        @Schema(description = "잔액", example = "300000")
        val balance: Long
    )
}
