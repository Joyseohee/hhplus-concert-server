package kr.hhplus.be.server.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.model.QueueToken.Companion.MAX_ACTIVE_COUNT
import java.time.Instant
import java.time.temporal.ChronoUnit

class QueueTokenTest : FreeSpec({
	"토큰 활성화" - {
		"토큰 만료 시간이 지나지 않았고 순번이 최대 접속 인원을 초과하지 않으면 활성화된다" {
			val now = Instant.now()

			val token = QueueToken.create(
				userId = 1L,
				expiresAt = now.plus(1L, ChronoUnit.MINUTES),
				status = QueueToken.Status.WAITING
			)

			listOf(0, 1).forEach { number ->
				val activatedToken = token.activate(MAX_ACTIVE_COUNT - number)
				activatedToken.status shouldBe QueueToken.Status.ACTIVE
				activatedToken.expiresAt shouldBeGreaterThanOrEqualTo now.plus(QueueToken.HOLD_TTL, ChronoUnit.MINUTES)
			}
		}

		"토큰 만료 시간이 지나지 않았지만 순번이 최대 접속 인원을 초과하면 그대로 반환한다" {
			val now = Instant.now()
			val inactivatedToken1 = QueueToken.create(
				userId = 1L,
				expiresAt = now.plus(1L, ChronoUnit.MINUTES),
				status = QueueToken.Status.WAITING
			)
			val inactivatedToken2 = QueueToken.create(
				userId = 2L,
				expiresAt = now.plus(1L, ChronoUnit.MINUTES),
				status = QueueToken.Status.WAITING
			)

			listOf(inactivatedToken1, inactivatedToken2).forEach { inactivatedToken ->
				inactivatedToken.activate(MAX_ACTIVE_COUNT + 1)

				inactivatedToken.status shouldBe QueueToken.Status.WAITING
				inactivatedToken.expiresAt shouldBeGreaterThanOrEqualTo now
			}
		}

		"토큰 만료 시간이 지났다면 순번이 최대 접속 인원을 초과하지 않았지만 예외가 발생한다" {
			val token = QueueToken.create(
				userId = 1L,
				expiresAt = Instant.now().minus(1L, ChronoUnit.MINUTES),
				status = QueueToken.Status.WAITING
			)

			shouldThrow<IllegalArgumentException> {
				token.activate(MAX_ACTIVE_COUNT - 1)
			}
		}
	}

	"토큰 만료" - {
		"토큰 만료 시간이거나 상태가 EXPIRED인 경우 만료 처리된다" {
			val timeExpiredToken = QueueToken.create(
				userId = 1L,
				expiresAt = Instant.now().minus(1L, ChronoUnit.MINUTES),
				status = QueueToken.Status.WAITING
			)

			val statusExpiredToken = QueueToken.create(
				userId = 1L,
				expiresAt = Instant.now().plus(1L, ChronoUnit.MINUTES),
				status = QueueToken.Status.EXPIRED
			)

			listOf(timeExpiredToken, statusExpiredToken).forEach { token ->
				val expiredToken = token.expire()
				expiredToken.status shouldBe QueueToken.Status.EXPIRED
			}
		}

		"토큰 만료 시간이 지나지 않고 상태도 EXPIRED가 아닌 경우 그대로 반환한다" {
			val now = Instant.now()
			val couldNotExpiredToken = QueueToken.create(
				userId = 1L,
				expiresAt = now.plus(1L, ChronoUnit.MINUTES),
				status = QueueToken.Status.WAITING
			)

			val nonExpiredToken = couldNotExpiredToken.expire()
			nonExpiredToken.status shouldBe QueueToken.Status.WAITING
			nonExpiredToken.expiresAt shouldBeGreaterThanOrEqualTo now
		}
	}
})

