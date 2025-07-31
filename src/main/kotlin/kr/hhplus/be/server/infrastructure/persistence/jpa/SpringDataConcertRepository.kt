package kr.hhplus.be.server.infrastructure.persistence.jpa

import kr.hhplus.be.server.domain.model.Concert
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataConcertRepository : JpaRepository<Concert, Long> {
    fun findAllByOrderByShowDateTimeDesc(): List<Concert>

}
