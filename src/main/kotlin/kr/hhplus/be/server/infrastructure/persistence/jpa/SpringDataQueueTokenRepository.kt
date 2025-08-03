package kr.hhplus.be.server.infrastructure.persistence.jpa

import kr.hhplus.be.server.domain.model.QueueToken
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface SpringDataQueueTokenRepository : JpaRepository<QueueToken, Long> {

	fun findByUserIdAndStatusNot(userId: Long, status: QueueToken.Status): QueueToken?

	fun findByTokenAndStatusNotAndExpiresAtAfter(
		token: String,
		status: QueueToken.Status,
		now: Instant
	): QueueToken?

	@Query("SELECT q FROM QueueToken q WHERE q.status = :status AND q.expiresAt > :expiresAt ORDER BY q.createdAt ASC")
	fun findAllByStatusAndExpiresAtAfterOrderByCreatedAt(
		status: QueueToken.Status,
		expiresAt: Instant,
		pageable: Pageable
	): List<QueueToken>

	fun countByStatusAndTokenIdLessThan(status: QueueToken.Status, id: Long): Int

	fun countByStatus(status: QueueToken.Status): Int

	fun findAllByExpiresAtBeforeAndStatusNot(
		expiresAt: Instant,
		status: QueueToken.Status
	): List<QueueToken>

}
