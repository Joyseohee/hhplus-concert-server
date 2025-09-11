package kr.hhplus.be.server.presentation

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.hhplus.be.server.application.validation.ValidateQueueTokenService
import kr.hhplus.be.server.application.validation.ValidateUserService
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ValidateInterceptor(
    private val tokenService: ValidateQueueTokenService,
    private val userService: ValidateUserService
) : HandlerInterceptor {
    override fun preHandle(r: HttpServletRequest, s: HttpServletResponse, h: Any): Boolean {
        val userValidationPaths = setOf("/api/v1/queue/token", "/api/v1/queue/token/status", "/api/v1/balance", "/api/v1/balance/charge")
        val noValidatePaths = setOf("/api/v1/queue/token/status", "/api/v1/users", "/api/v1/reservations/concerts/for-test", "/api/v1/reservations/concerts/seats")

        if(r.requestURI in noValidatePaths) {
            return true
        } else if (r.requestURI in userValidationPaths) {
            val id = r.getHeader("User-Id")?.toLongOrNull()
                ?: throw IllegalAccessException("User-Id 오류")
            userService.validateUser(id)
            r.setAttribute("currentUserId", id)
        } else {
            val token = r.getHeader("Queue-Token")
                ?: throw IllegalAccessException("Queue-Token 없음")
            val id = tokenService.validateToken(token)
            r.setAttribute("currentUserId", id)
        }
        return true
    }
}