package kr.hhplus.be.server.infrastructure.persistence.jpa

import kr.hhplus.be.server.domain.model.UserBalance
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataUserBalanceRepository : JpaRepository<UserBalance, Long> {
}
