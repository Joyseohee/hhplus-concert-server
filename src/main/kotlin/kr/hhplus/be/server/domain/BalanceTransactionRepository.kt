package kr.hhplus.be.server.domain

interface BalanceTransactionRepository {
	fun findById(id: Long): BalanceTransaction?
	fun save(balanceTransaction: BalanceTransaction): BalanceTransaction
}