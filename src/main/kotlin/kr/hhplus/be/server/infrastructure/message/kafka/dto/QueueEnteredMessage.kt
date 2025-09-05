package kr.hhplus.be.server.infrastructure.message.kafka.dto

data class QueueEnteredMessage(
	val userId: Long = 0L,
)