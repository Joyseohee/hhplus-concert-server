package kr.hhplus.be.server.application

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.model.Reservation
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import kr.hhplus.be.server.domain.repository.ReservationRepository
import kr.hhplus.be.server.domain.repository.SeatHoldRepository
import kr.hhplus.be.server.domain.repository.SeatRepository
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ConfirmReservationUseCase(
	private val seatRepository: SeatRepository,
	private val seatHoldRepository: SeatHoldRepository,
	private val reservationRepository: ReservationRepository,
	private val userBalanceRepository: UserBalanceRepository,
	private val queueTokenRepository: QueueTokenRepository,
) {
	fun confirmReservation(
		userId: Long,
		input: Input
	): Output {
		val seatHold = seatHoldRepository.findValidSeatHoldBySeatId(userId = userId, seatId = input.seatId)
			?: throw IllegalArgumentException("유효하지 않는 좌석 점유 요청입니다. 좌석 ID: ${input.seatId}")

		val seat = seatRepository.findById(seatHold.seatId)
			?: throw IllegalArgumentException("존재하지 않는 좌석입니다. 좌석 ID: ${seatHold.seatId}")

		val userBalance = userBalanceRepository.findById(userId)
			?: throw IllegalArgumentException("사용자 잔액을 찾을 수 없습니다. 사용자 ID: $userId")

		userBalanceRepository.save(
			userBalance.use(seat.price)
		)

		val reservation = Reservation.reserve(
			reservationUuid = input.reservationUuid,
			userId = userId,
			concertId = seatHold.concertId,
			seatId = seatHold.seatId,
			reservedAt = Instant.now(),
			price = seat.price
		)

		// 예약 확정
		val confirmedReservation = reservationRepository.save(reservation)

		// 좌석 점유 만료
		seatHoldRepository.deleteById(seatHold)

		// 토큰 만료
		val queueToken = queueTokenRepository.findByUserId(userId)
			?: throw IllegalArgumentException("사용자 토큰을 찾을 수 없습니다. 사용자 ID: $userId")
		queueTokenRepository.deleteById(queueToken.tokenId!!)

		return Output(
			concertId = confirmedReservation.concertId,
			seatId = confirmedReservation.seatId,
			price = confirmedReservation.price
		)
	}

	@Schema(name = "ConfirmReservationRequest", description = "예약 확정 요청")
	data class Input(
		@Schema(description = "예약 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
		val reservationUuid: String,
		@Schema(description = "점유 요청 ID", example = "0e02b2c3d479", requiredMode = Schema.RequiredMode.REQUIRED)
		val seatId: Long,
	)

	@Schema(name = "ConfirmReservationResponse", description = "예약 확정 응답")
	data class Output(
		@Schema(description = "콘서트 ID", example = "1")
		val concertId: Long,
		@Schema(description = "좌석 id", example = "1")
		val seatId: Long,
		@Schema(description = "결제 가격", example = "130000")
		val price: Long,
	)
}
