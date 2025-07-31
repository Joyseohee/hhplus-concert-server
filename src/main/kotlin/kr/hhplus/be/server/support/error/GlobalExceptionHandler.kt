package kr.hhplus.be.server.support.error

import io.swagger.v3.oas.annotations.Hidden
import kr.hhplus.be.server.support.ApiResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.Exception

@Hidden
@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
	// 공통 예외 처리
	@ExceptionHandler(CoreException::class)
	fun handleCoreException(
		ex: CoreException
	): ResponseEntity<ApiResponse<Any?>> {
		return ResponseEntity.status(ex.error.code).body(
			ApiResponse(
				code = "FAILED",
				message = ex.error.message,
				data = null
			)
		)
	}

	// 예: 잘못된 요청 바디(JSON 파싱 오류)
	override fun handleHttpMessageNotReadable(
		ex: HttpMessageNotReadableException,
		headers: HttpHeaders,
		status: HttpStatusCode,
		request: WebRequest
	): ResponseEntity<Any>? {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
			ApiResponse(
				code = "BAD_REQUEST",
				message = ex.message ?: "잘못된 요청 본문입니다.",
				data = null
			)
		)
	}

	// 공통 예외 처리 (필요하다면 오버라이드)
	override fun handleExceptionInternal(
		ex: Exception,
		body: Any?,
		headers: HttpHeaders,
		statusCode: HttpStatusCode,
		request: WebRequest
	): ResponseEntity<Any>? {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
			ApiResponse(
				code = "INTERNAL_SERVER_ERROR",
				message = ex.message ?: "서버 내부 오류가 발생했습니다.",
				data = null
			)
		)
	}

	@ExceptionHandler(IllegalAccessException::class)
	fun handleIllegal(
		ex: IllegalAccessException,
	): ResponseEntity<Any>? {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
			ApiResponse(
				code = "INTERNAL_SERVER_ERROR",
				message = ex.message ?: "서버 내부 오류가 발생했습니다.",
				data = null
			)
		)
	}
}