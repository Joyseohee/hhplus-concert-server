package kr.hhplus.be.server

import kr.hhplus.be.server.support.Response
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler

// @RestControllerAdvice
class GlobalExceptionHandler {

	@ExceptionHandler(NoSuchElementException::class)
	fun handleNotFound(ex: NoSuchElementException): ResponseEntity<Response<Nothing>> {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
			Response(
				code = "PRODUCT_NOT_FOUND",
				message = ex.message ?: "리소스를 찾을 수 없습니다.",
				data = null
			)
		)
	}

	@ExceptionHandler(Exception::class)
	fun handleGeneral(ex: Exception): ResponseEntity<Response<Nothing>> {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
			Response(
				code = "INTERNAL_ERROR",
				message = ex.message ?: "서버 내부 오류가 발생했습니다.",
				data = null
			)
		)
	}

}
