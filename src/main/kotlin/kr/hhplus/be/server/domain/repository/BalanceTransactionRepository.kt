package kr.hhplus.be.server.domain.repository

import kr.hhplus.be.server.domain.model.BalanceTransaction

interface BalanceTransactionRepository {
	fun findById(id: Long): BalanceTransaction?
	fun save(balanceTransaction: BalanceTransaction): BalanceTransaction
}