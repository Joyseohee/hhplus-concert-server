package kr.hhplus.be.server.infrastructure.message.kafka.dto

import org.springframework.kafka.support.Acknowledgment

data class PendingMessage<T>(
	val payload: T,
	val ack: Acknowledgment
)
