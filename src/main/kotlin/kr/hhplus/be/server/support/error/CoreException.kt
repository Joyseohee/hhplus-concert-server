package kr.hhplus.be.server.support.error

class CoreException(
	val error: CoreError
) : RuntimeException()