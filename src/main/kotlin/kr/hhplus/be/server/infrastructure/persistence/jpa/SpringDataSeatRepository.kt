package kr.hhplus.be.server.infrastructure.persistence.jpa

import kr.hhplus.be.server.domain.model.Seat
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataSeatRepository : JpaRepository<Seat, Long> {
}
