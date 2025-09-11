package kr.hhplus.be.server.application

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.model.Seat
import kr.hhplus.be.server.domain.repository.SeatRepository
import org.springframework.stereotype.Service

@Service
class CreateSeatUseCase(
	private val seatRepository: SeatRepository,
) {
	fun execute(seatNumber: Int): Output {
		val seat = seatRepository.save(Seat.create(seatNumber = seatNumber, price = 1000))

		return Output(
			seatId = seat.seatId!!,
			seatNumber = seat.seatNumber,
			price = seat.price,
		)
	}

	data class Input(val seatNumber: Int)

	@Schema(name = "CreateSeatRequest", description = "좌석 생성 요청")
	data class Output(
		@Schema(description = "좌석 id", example = "1")
		val seatId: Long,
		@Schema(description = "좌석 번호", example = "1")
		val seatNumber: Int,
		@Schema(description = "좌석 가격", example = "130000")
		val price: Long,
	)
}
