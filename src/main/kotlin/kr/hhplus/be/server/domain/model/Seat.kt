package kr.hhplus.be.server.domain.model

import jakarta.persistence.*
import kr.hhplus.be.server.infrastructure.persistence.jpa.BaseEntity

@Entity
@Table(name = "seats")
class Seat private constructor(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val seatId: Long? = null,
	@Column(name = "seat_number", nullable = false)
	val seatNumber: Int,
	@Column(name = "price", nullable = false)
	val price: Long,
) : BaseEntity() {

	init {
		require(seatNumber in 1..5000) { "좌석 번호는 1에서 5000 사이의 숫자다." }
	}

	companion object {
		fun create(
			seatId: Long? = null,
			seatNumber: Int,
			price: Long
		): Seat {
			return Seat(
				seatId = seatId,
				seatNumber = seatNumber,
				price = price
			)
		}
	}
}
