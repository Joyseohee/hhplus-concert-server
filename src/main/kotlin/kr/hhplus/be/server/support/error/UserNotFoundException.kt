package kr.hhplus.be.server.support.error

class UserNotFoundException : RuntimeException {
	constructor() : super("사용자를 찾을 수 없습니다.")
	constructor(message: String) : super(message)
	constructor(cause: Throwable) : super("사용자를 찾을 수 없습니다.", cause)
	constructor(message: String, cause: Throwable) : super(message, cause)
}