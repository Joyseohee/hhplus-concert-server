package kr.hhplus.be.server.support.error

class TokenNotFoundException : RuntimeException {
	constructor() : super("토큰을 찾을 수 없습니다.")
	constructor(message: String) : super(message)
	constructor(cause: Throwable) : super("토큰을 찾을 수 없습니다.", cause)
	constructor(message: String, cause: Throwable) : super(message, cause)
}