package kr.hhplus.be.server.service

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.*
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ConfirmReservationService(
    private val seatRepository: SeatRepository,
    private val seatHoldRepository: SeatHoldRepository,
    private val reservationRepository: ReservationRepository,
    private val userBalanceRepository: UserBalanceRepository
) {
    fun confirmReservation(
        userId: Long,
        input: Input
    ): Output {
        // 중복 시도 방지
        if (reservationRepository.findByUuid(input.reservationUuid) != null) {
            throw IllegalArgumentException("중복된 요청입니다.")
        }

        // 좌석 점유 확인 로직
        val seatHold = seatHoldRepository.findBySeatId(input.seatId)
            ?: throw IllegalArgumentException("유효하지 않은 좌석 예약입니다.")

        // 좌석 점유 상태 갱신
        val checkedSeatHold = seatHold.expireIfNeeded()
        if (checkedSeatHold.status == SeatHold.Status.EXPIRED && checkedSeatHold.status != SeatHold.Status.EXPIRED) {
            seatHoldRepository.save(checkedSeatHold)
        }
        checkedSeatHold.isValid(userId)

        // 좌석 정보 확인
        val seat = seatRepository.findById(checkedSeatHold.seatId)
            ?: throw IllegalArgumentException("존재하지 않는 좌석입니다.")

        // 사용자 검증 및 결제
        val userBalance = userBalanceRepository.findById(checkedSeatHold.userId)
            ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        userBalanceRepository.save(
            userBalance.use(seat.price)
        )

        // 예약 확정
        val reservation = reservationRepository.save(
            Reservation.create(
                reservationUuid = input.reservationUuid,
                userId = checkedSeatHold.userId,
                concertId = checkedSeatHold.concertId,
                seatId = checkedSeatHold.seatId,
                reservedAt = Instant.now(),
                price = seat.price
            )
        )

        // 점유상태 업데이트
        seatHoldRepository.save(
            SeatHold.create(
                seatHoldId = seatHold.seatHoldId,
                seatHoldUuid = seatHold.seatHoldUuid,
                userId = seatHold.userId,
                concertId = seatHold.concertId,
                seatId = seatHold.seatId,
                expiresAt = seatHold.expiresAt,
                status = SeatHold.Status.RESERVED
            )
        )

        return Output(
            concertId = seatHold.concertId,
            seatId = reservation.seatId,
            price = reservation.price
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
