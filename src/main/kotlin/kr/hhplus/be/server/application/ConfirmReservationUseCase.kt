package kr.hhplus.be.server.application

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.transaction.Transactional
import kr.hhplus.be.server.domain.model.Reservation
import kr.hhplus.be.server.domain.repository.*
import kr.hhplus.be.server.support.annotation.RedisLock
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ConfirmReservationUseCase(
	private val seatRepository: SeatRepository,
	private val seatHoldRepository: SeatHoldRepository,
	private val reservationRepository: ReservationRepository,
	private val userBalanceRepository: UserBalanceRepository,
	private val queueTokenRepository: QueueTokenRepository,
	private val concertAggregationRepository: ConcertAggregationRepository,
) {

	@RedisLock(
		key = "'lock:balance:{' + #userId + '}'",
		waitTimeMs = 100, leaseTimeMs = 2000, failFast = false
	)
	@Transactional
	fun confirmReservation(
		userId: Long,
		input: Input
	): Output {
		val seatHold = seatHoldRepository.findByUserIdAndUuid(userId = userId, seatHoldUuid = input.seatHoldUuid)
			?: throw IllegalArgumentException("유효하지 않는 좌석 점유 요청입니다. 좌석 ID: ${input.seatHoldUuid}")

		val seat = seatRepository.findById(seatHold.seatId)
			?: throw IllegalArgumentException("존재하지 않는 좌석입니다. 좌석 ID: ${seatHold.seatId}")

		val userBalance = userBalanceRepository.findById(userId)
			?: throw IllegalArgumentException("사용자 잔액을 찾을 수 없습니다. 사용자 ID: $userId")

		// 잔액 차감
		userBalance.use(seat.price)

		// 예약 확정
		val confirmedReservation = reservationRepository.save(Reservation.reserve(
			reservationUuid = input.reservationUuid,
			userId = userId,
			concertId = seatHold.concertId,
			seatId = seatHold.seatId,
			reservedAt = Instant.now(),
			price = seat.price
		))

		// region - 추후 비동기 처리로 변경. 좌석 점유 만료 및 토큰 만료는 예약 확정 후에 처리합니다.
		concertAggregationRepository.incrementScore("popular:concerts", seatHold.concertId)

		seatHoldRepository.deleteById(seatHold)

		// 토큰 만료
		val queueToken = queueTokenRepository.findByUserId(userId)
			?: throw IllegalArgumentException("사용자 토큰을 찾을 수 없습니다. 사용자 ID: $userId")

		queueTokenRepository.deleteById(queueToken.userId)
		// endregion

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
		@Schema(description = "점유 요청 UUID", example = "0e02b2c3d479", requiredMode = Schema.RequiredMode.REQUIRED)
		val seatHoldUuid: String,
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
