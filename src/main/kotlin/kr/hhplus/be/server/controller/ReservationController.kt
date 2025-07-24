package kr.hhplus.be.server.controller


import kr.hhplus.be.server.controller.swagger.SwaggerReservationController
import kr.hhplus.be.server.service.ConfirmReservationService
import kr.hhplus.be.server.service.HoldSeatService
import kr.hhplus.be.server.service.ListConcertService
import kr.hhplus.be.server.service.ListSeatService
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
)  {

    @GetMapping("/concerts")
     fun getConcerts(
//		token: String
	): ResponseEntity<ApiResponse<ListConcertService.Output>> {

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
     fun getSeats(
        //        token: String,
        @RequestHeader(value = "X-Client-Id", required = true) userId: Long,
        @PathVariable(required = true) concertId: Long
    ): ResponseEntity<ApiResponse<ListSeatService.Output>> {

        val seats = listSeatService.listAvailableSeats(concertId, userId)

        return ResponseEntity.ok(
            ApiResponse(
                code = "SUCCESS",
                message = "좌석 조회 성공",
                data = seats
            )
        )
    }


    @PostMapping("/concerts/{concertId}/seats/hold")
    fun holdSeats(
        //        token: String,
        @RequestHeader(value = "X-Client-Id", required = true) userId: Long,
        @RequestBody seatsHoldRequest: HoldSeatService.Input
    ): ResponseEntity<ApiResponse<HoldSeatService.Output>> {

        val holdSeat = holdSeatService.holdSeat(
            userId = userId,
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


}