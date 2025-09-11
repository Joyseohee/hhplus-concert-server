package kr.hhplus.be.server.presentation.controller

import jakarta.persistence.Id
import kr.hhplus.be.server.application.CreateUserUseCase
import kr.hhplus.be.server.application.RequestQueueTokenUseCase
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.presentation.CurrentUser
import kr.hhplus.be.server.support.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val createUserUseCase: CreateUserUseCase,
) {

    @PostMapping
    fun createUser(userId: Long, balance: Long): ResponseEntity<ApiResponse<CreateUserUseCase.Output>> {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                code = "SUCCESS",
                message = "토큰 생성 성공",
                data = createUserUseCase.createUser(userId, balance)
            )
        )
    }

}
