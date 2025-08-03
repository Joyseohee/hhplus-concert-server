package kr.hhplus.be.server.infrastructure.persistence.inmemory

import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class UserBalanceTable : UserBalanceRepository {
    private val table = ConcurrentHashMap<Long, UserBalance>()

    init {
        // Initialize with some dummy data
        table[1L] = UserBalance.create(userId = 1L, balance = 1000)
        table[2L] = UserBalance.create(userId = 2L, balance = 2000)
        table[3L] = UserBalance.create(userId = 3L, balance = 3000)
    }

    override fun findById(id: Long): UserBalance? {
        Thread.sleep(Math.random().toLong() * 200L)
        return table[id]
    }

    override fun save(
        userBalance: UserBalance
    ): UserBalance {
        Thread.sleep(Math.random().toLong() * 300L)
        val userId = userBalance.userId ?: table.keys.maxOrNull()?.plus(1) ?: 1L
        val userPoint = UserBalance.create(
            userId = userId,
            balance = userBalance.balance
        )
        table[userId] = userPoint
        return userPoint
    }

    override fun saveAll(userBalances: List<UserBalance>): List<UserBalance> {
        return userBalances.map { save(it) }
    }

    override fun clear() {
        table.clear()
    }
}