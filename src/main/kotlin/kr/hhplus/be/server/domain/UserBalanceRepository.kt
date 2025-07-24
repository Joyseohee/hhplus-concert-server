package kr.hhplus.be.server.domain

interface UserBalanceRepository {
	fun findById(id: Long?): UserBalance?

	fun save(userBalance: UserBalance): UserBalance

	fun clear()
}