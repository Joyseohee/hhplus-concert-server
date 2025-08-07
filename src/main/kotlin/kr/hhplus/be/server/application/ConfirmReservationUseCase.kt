package kr.hhplus.be.server.application

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.transaction.Transactional
import kr.hhplus.be.server.domain.model.Reservation
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import kr.hhplus.be.server.domain.repository.ReservationRepository
import kr.hhplus.be.server.domain.repository.SeatHoldRepository
import kr.hhplus.be.server.domain.repository.SeatRepository
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
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

	@Retryable(
		value = [ObjectOptimisticLockingFailureException::class],
		maxAttempts = 3,
		backoff = Backoff(delay = 200, multiplier = 2.0) // 100ms 간격으로 재시도
	)
	@Transactional
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
		println("예약 확정: 사용자 ID = $userId, 예약 UUID = ${input.reservationUuid}, 좌석 ID = ${input.seatId}, 가격 = ${seat.price}")

		// 잔액 차감
		userBalance.use(seat.price)
		println("잔액 차감: 사용자 ID = $userId, 좌석 ID = ${seat.seatId}, 차감 금액 = ${seat.price}, 잔액 = ${userBalance.balance}")

		//region - 좌석 점유 만료 및 토큰 만료는 예약 확정 후에 처리합니다.
		// 좌석 점유 만료
		seatHoldRepository.deleteById(seatHold)
		println("좌석 점유 만료: 사용자 ID = $userId, 좌석 ID = ${input.seatId}, 예약 UUID = ${input.reservationUuid}")

		// 토큰 만료
		val queueToken = queueTokenRepository.findByUserId(userId)
			?: throw IllegalArgumentException("사용자 토큰을 찾을 수 없습니다. 사용자 ID: $userId")

		println("토큰 확인: 사용자 ID = $userId, 토큰 ID = ${queueToken.tokenId}, 만료 시간 = ${queueToken.expiresAt}")

		queueTokenRepository.deleteById(queueToken.tokenId!!)
		println("토큰 만료: 사용자 ID = $userId, 토큰 ID = ${queueToken.tokenId}, 만료 시간 = ${queueToken.expiresAt}")
		// endregion

		return Output(
			concertId = confirmedReservation.concertId,
			seatId = confirmedReservation.seatId,
			price = confirmedReservation.price
		)
	}

	@Recover
	fun recover(e: ObjectOptimisticLockingFailureException, userId: Long, input: ChargeBalanceUseCase.Input): ChargeBalanceUseCase.Output {
		println("재시도 실패: 사용자 ID = $userId, 충전액 = ${input.amount}, 에러 = ${e.message}")
		throw RuntimeException("좌석 예약에 실패했습니다. 나중에 다시 시도해주세요.")
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
