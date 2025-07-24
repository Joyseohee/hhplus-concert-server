package kr.hhplus.be.server.support.error

import io.swagger.v3.oas.annotations.Hidden
import kr.hhplus.be.server.support.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@Hidden
@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

	@ExceptionHandler(NoSuchElementException::class)
	fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ApiResponse<Nothing>> {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
			ApiResponse(
				code = "PRODUCT_NOT_FOUND",
				message = ex.message ?: "리소스를 찾을 수 없습니다.",
				data = null
			)
		)
	}

	@ExceptionHandler(IllegalArgumentException::class)
	fun handlePolicyException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
			ApiResponse(
				code = "BAD_REQUEST",
				message = ex.message ?: "서버 내부 오류가 발생했습니다.",
				data = null
			)
		)
	}

	@ExceptionHandler(Exception::class)
	fun handleGeneral(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
			ApiResponse(
				code = "INTERNAL_ERROR",
				message = ex.message ?: "서버 내부 오류가 발생했습니다.",
				data = null
			)
		)
	}

}