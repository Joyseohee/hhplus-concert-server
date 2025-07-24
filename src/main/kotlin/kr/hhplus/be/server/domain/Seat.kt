package kr.hhplus.be.server.domain

data class Seat private constructor(
	val seatId: Long?,
	val seatNumber: Int,
	val price: Long,
) {

	init {
		require(seatNumber in 1..50) { "좌석 번호는 1에서 50 사이의 숫자다." }
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
