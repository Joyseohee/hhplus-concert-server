package kr.hhplus.be.server.support.error

class SeatHoldUnavailableException : RuntimeException {
	constructor() : super("다른 사용자가 이미 점유한 좌석입니다.")
	constructor(message: String) : super(message)
	constructor(cause: Throwable) : super("다른 사용자가 이미 점유한 좌석입니다.", cause)
	constructor(message: String, cause: Throwable) : super(message, cause)
}