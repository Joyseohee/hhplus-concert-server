package kr.hhplus.be.server.application

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.stereotype.Service

@Service
class CreateUserUseCase(
    private val userRepository: UserBalanceRepository
) {

    fun createUser(balance: Long): Output {
        val newUser = UserBalance.create(balance = balance)
        userRepository.save(newUser)

        return Output(
            userId = newUser.userId!!,
            balance = newUser.balance!!
        )
    }

    data class Input(val balance: Long)

    @Schema(name = "CreateUserUseCaseResponse", description = "사용자 생성 응답")
    data class Output(
        @Schema(description = "사용자 ID", example = "1")
        val userId: Long,
        @Schema(description = "잔고", example = "0")
        val balance: Long,
    )
}
