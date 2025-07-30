package kr.hhplus.be.server.controller


import kr.hhplus.be.server.controller.swagger.SwaggerReservationController
import kr.hhplus.be.server.service.ConfirmReservationService
import kr.hhplus.be.server.service.HoldSeatService
import kr.hhplus.be.server.service.ListConcertService
import kr.hhplus.be.server.service.ListSeatService
import kr.hhplus.be.server.service.validation.ValidateQueueTokenService
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
	val listConcertService: ListConcertService,
	val listSeatService: ListSeatService,
	val holdSeatService: HoldSeatService,
	val confirmReservationService: ConfirmReservationService,

	val validateQueueTokenService: ValidateQueueTokenService
) : SwaggerReservationController {

	@GetMapping("/concerts")
	override fun getConcerts(
		@RequestHeader(name = "Queue-Token", required = true) token: String
	): ResponseEntity<ApiResponse<ListConcertService.Output>> {
		val validateToken = validateQueueTokenService.validateToken(token)

		val concerts = listConcertService.listConcerts()

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
	): ResponseEntity<ApiResponse<ListSeatService.Output>> {
		val validateToken = validateQueueTokenService.validateToken(token)

		val seats = listSeatService.listAvailableSeats(concertId, validateToken.userId)

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
		@RequestBody seatsHoldRequest: HoldSeatService.Input
	): ResponseEntity<ApiResponse<HoldSeatService.Output>> {
		val validateToken = validateQueueTokenService.validateToken(token)

		val holdSeat = holdSeatService.holdSeat(
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
		@RequestBody reservationRequest: ConfirmReservationService.Input
	): ResponseEntity<ApiResponse<ConfirmReservationService.Output>> {
		val validateToken = validateQueueTokenService.validateToken(token)

		val reservation = confirmReservationService.confirmReservation(
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