package kr.hhplus.be.server.presentation.controller

import kr.hhplus.be.server.application.CreateUserUseCase
import kr.hhplus.be.server.support.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val createUserUseCase: CreateUserUseCase,
) {

    @PostMapping
    fun createUser(
        @RequestBody request: CreateUserUseCase.Input
    ): ResponseEntity<ApiResponse<CreateUserUseCase.Output>> {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                code = "SUCCESS",
                message = "토큰 생성 성공",
                data = createUserUseCase.createUser(request.balance)
            )
        )
    }

}
