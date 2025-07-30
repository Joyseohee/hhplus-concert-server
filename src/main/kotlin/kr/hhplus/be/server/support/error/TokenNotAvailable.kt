package kr.hhplus.be.server.support.error

class TokenNotAvailable : RuntimeException {
	constructor() : super("토큰이 사용 불가능합니다.")
	constructor(message: String) : super(message)
	constructor(cause: Throwable) : super("토큰이 사용 불가능합니다.", cause)
	constructor(message: String, cause: Throwable) : super(message, cause)
}