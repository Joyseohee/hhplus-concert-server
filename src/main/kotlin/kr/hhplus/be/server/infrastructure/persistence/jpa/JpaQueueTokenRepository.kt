package kr.hhplus.be.server.infrastructure.persistence.jpa

import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
@Primary
class JpaQueueTokenRepository(
	private val repository: SpringDataQueueTokenRepository
) : QueueTokenRepository {
	override fun findByUserId(userId: Long): QueueToken? {
		return repository.findByUserIdAndStatusNot(userId, QueueToken.Status.EXPIRED)
	}

	override fun findByToken(token: String): QueueToken? {
		return repository.findByTokenAndStatusNotAndExpiresAtAfter(
			token,
			QueueToken.Status.EXPIRED,
			Instant.now()
		)
	}

	override fun findValidatedByToken(token: String): QueueToken? {
		return repository.findByTokenAndStatusNotAndExpiresAtAfter(token, QueueToken.Status.EXPIRED, Instant.now())
	}

	override fun findActiveByToken(token: String): QueueToken? {
		return repository.findByTokenAndStatusAndExpiresAtAfter(
			token,
			QueueToken.Status.ACTIVE,
			Instant.now()
		)
	}

	override fun findAllWaitingTokenForActivate(i: Int): List<QueueToken> {
		return repository.findAllByStatusAndExpiresAtAfterOrderByCreatedAt(
			QueueToken.Status.WAITING,
			Instant.now(),
		)
	}

	override fun findAllActivated(): List<QueueToken> {
		return repository.findAllByStatusAndExpiresAtAfterOrderByCreatedAt(
			QueueToken.Status.ACTIVE,
			Instant.now(),
		)
	}

	override fun findAll(): List<QueueToken> {
		return repository.findAllByOrderByCreatedAtAsc()
	}

	override fun findPositionById(id: Long): Int? {
		return repository.countByStatusAndTokenIdLessThan(QueueToken.Status.WAITING, id) + 1
	}

	override fun findTokensToExpire(): List<QueueToken> {
		return repository.findAllByExpiresAtBeforeAndStatusNot(
			Instant.now(),
			QueueToken.Status.EXPIRED
		)
	}

	override fun countByStatus(active: QueueToken.Status): Int {
		return repository.countByStatus(active)
	}

	override fun save(queueToken: QueueToken): QueueToken {
		return repository.save(queueToken)
	}

	override fun saveAll(queueTokens: List<QueueToken>): List<QueueToken> {
		return repository.saveAll(queueTokens)
	}

	override fun deleteById(id: Long) {
		repository.deleteById(id)
	}

	override fun deleteByIds(ids: List<Long>) {
		return repository.deleteAllById(ids)
	}

	override fun clear() {
		repository.deleteAll()
	}

}