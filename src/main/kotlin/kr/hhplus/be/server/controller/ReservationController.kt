package kr.hhplus.be.server.controller


import kr.hhplus.be.server.controller.swagger.SwaggerReservationController
import kr.hhplus.be.server.application.ConfirmReservationUseCase
import kr.hhplus.be.server.application.HoldSeatUseCase
import kr.hhplus.be.server.application.ListConcertUseCase
import kr.hhplus.be.server.application.ListSeatUseCase
import kr.hhplus.be.server.application.validation.ValidateQueueTokenService
import kr.hhplus.be.server.support.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(
	val listConcertUseCase: ListConcertUseCase,
	val listSeatUseCase: ListSeatUseCase,
	val holdSeatUseCase: HoldSeatUseCase,
	val confirmReservationUseCase: ConfirmReservationUseCase,

	val validateQueueTokenService: ValidateQueueTokenService
) : SwaggerReservationController {

	@GetMapping("/concerts")
	override fun getConcerts(
		@RequestHeader(name = "Queue-Token", required = true) token: String
	): ResponseEntity<ApiResponse<ListConcertUseCase.Output>> {
		val validateToken = validateQueueTokenService.validateToken(token)

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
		@RequestHeader(name = "Queue-Token", required = true) token: String,
		@PathVariable(required = true) concertId: Long
	): ResponseEntity<ApiResponse<ListSeatUseCase.Output>> {
		val validateToken = validateQueueTokenService.validateToken(token)

		val seats = listSeatUseCase.listAvailableSeats(concertId, validateToken.userId)

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
        @RequestHeader(name = "Queue-Token", required = true) token: String,
		@RequestBody seatsHoldRequest: HoldSeatUseCase.Input
	): ResponseEntity<ApiResponse<HoldSeatUseCase.Output>> {
		val validateToken = validateQueueTokenService.validateToken(token)

		val holdSeat = holdSeatUseCase.holdSeat(
			userId = validateToken.userId,
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

	@PostMapping("/")
	override fun confirmedReservation(
        @RequestHeader(name = "Queue-Token", required = true) token: String,
		@RequestBody reservationRequest: ConfirmReservationUseCase.Input
	): ResponseEntity<ApiResponse<ConfirmReservationUseCase.Output>> {
		val validateToken = validateQueueTokenService.validateToken(token)

		val reservation = confirmReservationUseCase.confirmReservation(
			userId = validateToken.userId,
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