package kr.hhplus.be.server.infrastructure.persistence.inmemory

import kr.hhplus.be.server.domain.model.BalanceTransaction
import kr.hhplus.be.server.domain.repository.BalanceTransactionRepository
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class BalanceTransactionsTable : BalanceTransactionRepository {
    private val table = ConcurrentHashMap<Long, BalanceTransaction>()

    override fun findById(id: Long): BalanceTransaction? {
        Thread.sleep(Math.random().toLong() * 200L)
        return table[id]
    }

    override fun save(
        balanceTransaction: BalanceTransaction
    ): BalanceTransaction {
        Thread.sleep(Math.random().toLong() * 300L)
        val balanceTxId = balanceTransaction.balanceTxId ?: table.keys.maxOrNull()?.plus(1) ?: 1L
        val balanceTransaction = BalanceTransaction.create(
            balanceTxId = balanceTransaction.balanceTxId,
            userId = balanceTransaction.userId,
            amount = balanceTransaction.amount,
            type = balanceTransaction.type,
            description = balanceTransaction.description,
        )

        table[balanceTransaction.balanceTxId] = balanceTransaction
        return balanceTransaction
    }
}