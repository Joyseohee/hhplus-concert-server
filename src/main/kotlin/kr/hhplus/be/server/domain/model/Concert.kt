package kr.hhplus.be.server.domain.model

import jakarta.persistence.*
import kr.hhplus.be.server.infrastructure.persistence.jpa.BaseEntity
import java.time.Instant

@Entity
@Table(name = "concerts")
class Concert private constructor(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val concertId: Long? = null,
	@Column(name = "title", nullable = false)
	val title: String,
	@Column(name = "venue", nullable = false)
	val venue: String,
	@Column(name = "show_datetime", nullable = false)
	val showDateTime: Instant,
) : BaseEntity() {

	companion object {
		fun create(
			concertId: Long? = null,
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
