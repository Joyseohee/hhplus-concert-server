package kr.hhplus.be.server.domain.repository

import kr.hhplus.be.server.domain.model.UserBalance

interface UserBalanceRepository {
	fun findById(id: Long?): UserBalance?

	fun save(userBalance: UserBalance): UserBalance

	fun clear()
}