package kr.hhplus.be.server.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.hhplus.be.server.infrastructure.persistence.jpa.BaseEntity

@Entity
@Table(name = "seats")
@SequenceGenerator(
	name = "seat_seq",
	sequenceName = "seat_id_seq",
	allocationSize = 1
)
class Seat private constructor(
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seat_seq")
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
