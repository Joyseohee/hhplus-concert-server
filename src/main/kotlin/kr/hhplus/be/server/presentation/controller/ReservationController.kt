package kr.hhplus.be.server.presentation.controller


import kr.hhplus.be.server.application.ConfirmReservationUseCase
import kr.hhplus.be.server.application.HoldSeatUseCase
import kr.hhplus.be.server.application.ListConcertUseCase
import kr.hhplus.be.server.application.ListSeatUseCase
import kr.hhplus.be.server.presentation.CurrentUser
import kr.hhplus.be.server.presentation.controller.swagger.SwaggerReservationController
import kr.hhplus.be.server.support.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(
	val listConcertUseCase: ListConcertUseCase,
	val listSeatUseCase: ListSeatUseCase,
	val holdSeatUseCase: HoldSeatUseCase,
	val confirmReservationUseCase: ConfirmReservationUseCase,
) : SwaggerReservationController {

	@GetMapping("/concerts")
	override fun getConcerts(): ResponseEntity<ApiResponse<ListConcertUseCase.Output>> {
		val concerts = listConcertUseCase.listConcerts()

		return ResponseEntity.ok(
			ApiResponse(
				code = "SUCCESS",
				message = "조회 성공",
				data = concerts
			)
		)
	}

	@GetMapping("/concerts/{concertId}/seats")
	override fun getSeats(
		@CurrentUser userId: Long,
		@PathVariable(required = true) concertId: Long
	): ResponseEntity<ApiResponse<ListSeatUseCase.Output>> {
		val seats = listSeatUseCase.listAvailableSeats(concertId, userId!!)

		return ResponseEntity.ok(
			ApiResponse(
				code = "SUCCESS",
				message = "좌석 조회 성공",
				data = seats
			)
		)
	}

	@PostMapping("/concerts/{concertId}/seats/hold")
	override fun holdSeats(
		@CurrentUser userId: Long,
		@RequestBody seatsHoldRequest: HoldSeatUseCase.Input
	): ResponseEntity<ApiResponse<HoldSeatUseCase.Output>> {

		val holdSeat = holdSeatUseCase.holdSeat(
			userId = userId!!,
			input = seatsHoldRequest,
		)

		return ResponseEntity.ok(
			ApiResponse(
				code = "SUCCESS",
				message = "좌석 점유 성공",
				data = holdSeat
			)
		)
	}

	@PostMapping
	override fun confirmedReservation(
		@CurrentUser userId: Long,
		@RequestBody reservationRequest: ConfirmReservationUseCase.Input
	): ResponseEntity<ApiResponse<ConfirmReservationUseCase.Output>> {
		val reservation = confirmReservationUseCase.confirmReservation(
			userId = userId!!,
			input = reservationRequest
		)
		return ResponseEntity.ok(
			ApiResponse(
				code = "SUCCESS",
				message = "예약 확정 성공",
				data = reservation
			)
		)
	}
}