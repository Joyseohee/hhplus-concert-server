package kr.hhplus.be.server.application

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.model.SeatHold
import kr.hhplus.be.server.domain.repository.ReservationRepository
import kr.hhplus.be.server.domain.repository.SeatHoldRepository
import kr.hhplus.be.server.domain.repository.SeatRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class HoldSeatUseCase(
	private val seatRepository: SeatRepository,
	private val seatHoldRepository: SeatHoldRepository,
	private val reservationRepository: ReservationRepository,
) {

	fun holdSeat(input: Input, userId: Long): Output {
		val seat = seatRepository.findById(input.seatId)
			?: throw IllegalArgumentException("존재하지 않는 좌석입니다. 좌석 ID: ${input.seatId}")

		reservationRepository.findBySeatId(input.seatId)
			?.let { throw IllegalArgumentException("이미 예약된 좌석입니다. 좌석 ID: ${input.seatId}") }

		val newSeatHold = SeatHold.held(
			seatHoldUuid = input.seatHoldUuid,
			userId = userId,
			concertId = input.concertId,
			seatId = seat.seatId!!
		)

		// todo - 동시성 문제 발생 가능 지점 : 좌석 + 콘서트에 unique constraint 필요, 점유 만료 후 delete 처리 필요
		val confirmedSeatHold = seatHoldRepository.save(newSeatHold)

		return Output(
			seatHoldUuid = confirmedSeatHold.seatHoldUuid,
			seatId = confirmedSeatHold.seatId,
			expiresAt = confirmedSeatHold.expiresAt
		)
	}

	@Schema(name = "HoldSeatRequest", description = "좌석 점유 요청")
	data class Input(
		@Schema(description = "점유 요청 ID", example = "0e02b2c3d479", requiredMode = Schema.RequiredMode.REQUIRED)
		val seatHoldUuid: String,
		@Schema(description = "콘서트 id", example = "1")
		val concertId: Long,
		@Schema(description = "좌석 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
		val seatId: Long
	)

	@Schema(name = "HoldSeatResponse", description = "좌석 점유 응답")
	data class Output(
		@Schema(description = "점유 요청 ID", example = "0e02b2c3d479", requiredMode = Schema.RequiredMode.REQUIRED)
		val seatHoldUuid: String,
		@Schema(description = "좌석 ID", example = "VALID")
		val seatId: Long,
		@Schema(description = "점유 만료까지 남은 시각", example = "300000")
		val expiresAt: Instant,
	)
}
