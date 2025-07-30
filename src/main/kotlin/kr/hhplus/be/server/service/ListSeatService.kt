package kr.hhplus.be.server.service

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.ConcertRepository
import kr.hhplus.be.server.domain.SeatHoldRepository
import kr.hhplus.be.server.domain.SeatRepository
import org.springframework.stereotype.Service

@Service
class ListSeatService(
	private val concertRepository: ConcertRepository,
	private val seatRepository: SeatRepository,
	private val seatHoldRepository: SeatHoldRepository
) {
	fun listAvailableSeats(concertId: Long, userId: Long): Output {
		val concert = concertRepository.findById(concertId)
			?: throw IllegalArgumentException("콘서트를 찾을 수 없습니다: concertId=$concertId")

		val seatHolds = seatHoldRepository.findAllByConcertId(concert.concertId)

		val seatHoldMap = seatHolds.associateBy { it.seatId }

		val seat = seatRepository.findAll()

		if (seat.isEmpty()) {
			return Output(
				concertId = concertId,
				availableSeats = emptyList()
			)
		}

		return Output(
			concertId = concertId,
			availableSeats = seat.map { seat ->
				val seatHold = seatHoldMap[seat.seatId]
				Output.SeatInfo(
					seatId = seat.seatId!!,
					seatNumber = seat.seatNumber,
					price = seat.price,
					isAvailable = seatHold?.isAvailable(userId) ?: true,
				)
			}.sortedBy { it.seatNumber }
		)
	}

	@Schema(name = "ListSeatRequest", description = "좌석 목록 조회 요청")
	data class Output(
		@Schema(description = "콘서트 ID", example = "1")
		val concertId: Long,
		@Schema(description = "좌석 목록")
		val availableSeats: List<SeatInfo>
	) {
		data class SeatInfo(
			@Schema(description = "좌석 id", example = "1")
			val seatId: Long,
			@Schema(description = "좌석 번호", example = "1")
			val seatNumber: Int,
			@Schema(description = "좌석 가격", example = "130000")
			val price: Long,
			@Schema(description = "예약 가능 여부", example = "true")
			val isAvailable: Boolean,
		)
	}

}
