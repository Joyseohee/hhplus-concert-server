package kr.hhplus.be.server.domain

data class BalanceTransaction private constructor(
	val balanceTxId: Long,
	val userId: Long,
	val amount: Long,
	val type: String,
	val description: String
) {
	companion object {
		fun create(
			balanceTxId: Long,
			userId: Long,
			amount: Long,
			type: String,
			description: String
		): BalanceTransaction {
			return BalanceTransaction(balanceTxId, userId, amount, type, description)
		}
	}
}
