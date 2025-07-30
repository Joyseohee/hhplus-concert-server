package kr.hhplus.be.server.domain

import java.time.Instant

data class Reservation private constructor(
	val reservationId: Long?,
	val reservationUuid: String,
	val userId: Long,
	val concertId: Long,
	val seatId: Long,
	val reservedAt: Instant,
	val price: Long
) {
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
