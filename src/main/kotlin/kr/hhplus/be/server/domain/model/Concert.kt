package kr.hhplus.be.server.domain.model

import java.time.Instant

data class Concert private constructor(
	val concertId: Long,
	val title: String,
	val venue: String,
	val showDateTime: Instant,
) {
	companion object {
		fun create(
			concertId: Long,
			title: String,
			venue: String,
			showDateTime: Instant
		): Concert {
			return Concert(concertId, title, venue, showDateTime)
		}
	}

	fun isAvailable(): Boolean {
		return showDateTime.isAfter(Instant.now())
	}
}
