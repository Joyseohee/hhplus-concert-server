package kr.hhplus.be.server.support.error

import org.springframework.http.HttpStatus

interface CoreError {
	val code: HttpStatus
	val message: String
}