package kr.hhplus.be.server.domain.model

import jakarta.persistence.*
import kr.hhplus.be.server.infrastructure.persistence.jpa.BaseEntity
import java.time.Instant

@Entity
@Table(name = "reservations")
class Reservation private constructor(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val reservationId: Long? = null,
	@Column(name = "reservation_uuid", nullable = false, unique = true)
	val reservationUuid: String,
	@Column(name = "user_id", nullable = false)
	val userId: Long,
	@Column(name = "concert_id", nullable = false)
	val concertId: Long,
	@Column(name = "seat_id", nullable = false)
	val seatId: Long,
	@Column(name = "reserved_at", nullable = false)
	val reservedAt: Instant,
	@Column(name = "price", nullable = false)
	val price: Long
) : BaseEntity() {

	companion object {
		fun create(
			reservationId: Long? = null,
			reservationUuid: String,
			userId: Long,
			concertId: Long,
			seatId: Long,
			reservedAt: Instant,
			price: Long
		): Reservation {
			return Reservation(
				reservationId = reservationId,
				reservationUuid	= reservationUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				reservedAt = reservedAt,
				price = price
			)
		}

		fun reserve(
			reservationId: Long? = null,
			reservationUuid: String,
			userId: Long,
			concertId: Long,
			seatId: Long,
			reservedAt: Instant,
			price: Long
		): Reservation {
			if (reservationUuid.isBlank()) {
				throw IllegalArgumentException("Reservation UUID must not be blank")
			}

			if (price <= 0) {
				throw IllegalArgumentException("User ID must be greater than zero")
			}

			return create(
				reservationId = reservationId,
				reservationUuid = reservationUuid,
				userId = userId,
				concertId = concertId,
				seatId = seatId,
				reservedAt = reservedAt,
				price = price
			)
		}
	}
}
