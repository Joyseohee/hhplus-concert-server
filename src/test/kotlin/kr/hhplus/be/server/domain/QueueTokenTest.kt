package kr.hhplus.be.server.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import java.time.Instant

class QueueTokenTest : StringSpec({
    "만료 시간이 지나면 true를 반환한다" {
        val token = QueueToken.create(
            userId = 1L,
            expiresAt = Instant.now().minusSeconds(1),
            status = QueueToken.Status.WAITING
        )
        token.isExpired() shouldBe true
    }

    "만료 시간이 지나지 않았으면 false를 반환한다" {
        val token = QueueToken.create(
            userId = 1L,
            expiresAt = Instant.now().plusSeconds(60),
            status = QueueToken.Status.WAITING
        )
        token.isExpired() shouldBe false
    }

    "상태가 WAITING이면 true를 반환한다" {
        val token = QueueToken.create(
            userId = 1L,
            status = QueueToken.Status.WAITING
        )
        token.isWaiting() shouldBe true
    }

    "상태가 ACTIVE이면 true를 반환한다" {
        val token = QueueToken.create(
            userId = 1L,
            status = QueueToken.Status.ACTIVE
        )
        token.isActive() shouldBe true
    }

    "만료되었으면 예외를 던진다" {
        val token = QueueToken.create(
            userId = 1L,
            expiresAt = Instant.now().minusSeconds(1),
            status = QueueToken.Status.ACTIVE
        )
        shouldThrow<IllegalArgumentException> {
            token.isValid()
        }
    }

    "WAITING면 false를 반환한다" {
        val token = QueueToken.create(
            userId = 1L,
            status = QueueToken.Status.WAITING
        )

        token.isValid() shouldBe false
    }

    "만료되지 않고 ACTIVE면 true를 반환한다" {
        val token = QueueToken.create(
            userId = 1L,
            expiresAt = Instant.now().plusSeconds(60),
            status = QueueToken.Status.ACTIVE
        )
        token.isValid() shouldBe true
    }
})

