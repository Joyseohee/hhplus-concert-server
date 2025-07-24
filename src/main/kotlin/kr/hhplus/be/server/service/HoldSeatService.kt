package kr.hhplus.be.server.service

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.SeatHold
import kr.hhplus.be.server.domain.SeatHoldRepository
import kr.hhplus.be.server.domain.SeatRepository
import kr.hhplus.be.server.domain.UserBalanceRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class HoldSeatService(
	private val userBalanceRepository: UserBalanceRepository,
	private val seatRepository: SeatRepository,
	private val seatHoldRepository: SeatHoldRepository
) {

	fun holdSeat(input: Input, userId: Long): Output {

		userBalanceRepository.findById(userId)
			?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")

		// 중복된 요청 방지 로직
		seatHoldRepository.findByUuid(input.seatHoldUuid)
			?.let {
				throw IllegalArgumentException("중복된 요청입니다.")
			}

		seatHoldRepository.findBySeatId(input.seatId)
			?.let {
				throw IllegalArgumentException("이미 점유된 좌석입니다.")
			}


		val seat = seatRepository.findById(input.seatId)

		if (seat == null) {
			throw IllegalArgumentException("좌석이 존재하지 않습니다. 좌석 ID: ${input.seatId}")
		}

		val seatHold = seatHoldRepository.save(
			seatHold = SeatHold.create(
				seatHoldUuid = input.seatHoldUuid,
				userId = userId,
				concertId = input.concertId,
				seatId = input.seatId
			)
		)

		// 여기서는 단순히 출력값을 생성합니다.
		return Output(
			seatHoldUuid = seatHold.seatHoldUuid,
			seatId = seatHold.userId,
			expiresAt = seatHold.expiresAt
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
