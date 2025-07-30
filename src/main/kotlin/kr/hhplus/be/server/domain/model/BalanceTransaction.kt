package kr.hhplus.be.server.domain.model

data class BalanceTransaction private constructor(
	val balanceTxId: Long,
	val userId: Long,
	val amount: Long,
	val type: TransactionType,
	val description: String
) {
	enum class TransactionType {
		USE,
		CHARGE
	}


	companion object {
		fun create(
			balanceTxId: Long,
			userId: Long,
			amount: Long,
			type: TransactionType,
			description: String
		): BalanceTransaction {
			return BalanceTransaction(balanceTxId, userId, amount, type, description)
		}
	}
}

