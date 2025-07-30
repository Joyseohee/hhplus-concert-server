package kr.hhplus.be.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.ConfirmReservationUseCase
import kr.hhplus.be.server.application.HoldSeatUseCase
import kr.hhplus.be.server.application.ListConcertUseCase
import kr.hhplus.be.server.application.ListSeatUseCase
import kr.hhplus.be.server.application.validation.ValidateQueueTokenService
import kr.hhplus.be.server.support.error.GlobalExceptionHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant

@TestConfiguration
class ReserveMockConfig {
	@Bean fun holdSeatUseCase() = mockk<HoldSeatUseCase>(relaxed = true)
	@Bean fun confirmReservationUseCase() = mockk<ConfirmReservationUseCase>(relaxed = true)
	@Bean fun concertUseCase() = mockk<ListConcertUseCase>(relaxed = true)
	@Bean fun seatUseCase() = mockk<ListSeatUseCase>(relaxed = true)
	@Bean fun validateQueueTokenService() = mockk<ValidateQueueTokenService>(relaxed = true)
}

@WebMvcTest(ReservationController::class)
@Import(GlobalExceptionHandler::class, ReserveMockConfig::class)
class ReservationControllerTest @Autowired constructor(
	private val mockMvc: MockMvc,
	private val objectMapper: ObjectMapper,
	private val holdSeatUseCase: HoldSeatUseCase,
	private val confirmReservationUseCase: ConfirmReservationUseCase,
	private val concertUseCase: ListConcertUseCase,
	private val seatUseCase: ListSeatUseCase,
	private val validateQueueTokenService: ValidateQueueTokenService
) : BehaviorSpec({
	extension(SpringExtension)

	given("좌석 점유 요청이 있을 때") {
		`when`("유효한 HoldSeatUseCase.Input이 전송되면") {
			then("SUCCESS 응답을 반환한다") {
				val req = HoldSeatUseCase.Input("uuid", 1L, 1L)
				every { holdSeatUseCase.holdSeat(userId = 1L, input = req) } returns HoldSeatUseCase.Output(
					"uuid",
					1,
					Instant.parse("2025-07-20T19:12:34Z")
				)

				mockMvc.post("/api/v1/reservations/concerts/1/seats/hold") {
					header("Queue-Token", "valid-token")
					contentType = MediaType.APPLICATION_JSON
					content = objectMapper.writeValueAsString(req)
				}.andExpect {
					status { isOk() }
					jsonPath("$.code") { value("SUCCESS") }
				}
			}
		}
		`when`("유효하지 않은 HoldSeatUseCase.Input이 전송되면") {
			then("BAD_REQUEST 응답을 반환한다") {
				mockMvc.post("/api/v1/reservations/concerts/1/seats/hold") {
					header("Queue-Token", "valid-token")
					contentType = MediaType.APPLICATION_JSON
					content = "{}"
				}.andExpect {
					status { isBadRequest() }
				}
			}
		}
	}

	given("예약 확정 요청이 있을 때") {
		`when`("유효한 ConfirmReservationUseCase.Input이 전송되면") {
			then("SUCCESS 응답을 반환한다") {
				val req = ConfirmReservationUseCase.Input("res-uuid", 1L)
				every {
					confirmReservationUseCase.confirmReservation(
						userId = 1L,
						input = req
					)
				} returns ConfirmReservationUseCase.Output(1L, 1L, 130000)

				mockMvc.post("/api/v1/reservations/") {
					header("Queue-Token", "valid-token")
					contentType = MediaType.APPLICATION_JSON
					content = objectMapper.writeValueAsString(req)
				}.andExpect {
					status { isOk() }
					jsonPath("$.code") { value("SUCCESS") }
				}
			}
		}
		`when`("유효하지 않은 ConfirmReservationUseCase.Input이 전송되면") {
			then("BAD_REQUEST 응답을 반환한다") {
				mockMvc.post("/api/v1/reservations/") {
					header("Queue-Token", "valid-token")
					contentType = MediaType.APPLICATION_JSON
					content = "{}"
				}.andExpect {
					status { isBadRequest() }
				}
			}
		}
	}

	given("콘서트 조회 요청이 있을 때") {
		`when`("요청이 들어오면") {
			then("SUCCESS 응답을 반환한다") {
				every { concertUseCase.listConcerts() } returns ListConcertUseCase.Output(
					listOf(
						ListConcertUseCase.Output.ConcertInfo(
							concertId = 1L,
							concertTitle = "Test Concert",
							concertVenue = "Test Venue",
							concertDateTime = "2025-07-20T19:12:34Z",
							isAvailable = true,
						),
						ListConcertUseCase.Output.ConcertInfo(
							concertId = 2L,
							concertTitle = "Another Concert",
							concertVenue = "Another Venue",
							concertDateTime = "2025-08-20T19:12:34Z",
							isAvailable = true,
						)
					)
				)

				mockMvc.get("/api/v1/reservations/concerts") {
					header("Queue-Token", "valid-token")
					accept = MediaType.APPLICATION_JSON
				}.andExpect {
					status { isOk() }
					jsonPath("$.code") { value("SUCCESS") }
				}
			}
		}
	}

	given("getSeats API endpoint") {
		`when`("request for concertId 1 is made") {
			then("responds with SUCCESS") {
				every { seatUseCase.listAvailableSeats(concertId = 1L, userId = 1L) } returns ListSeatUseCase.Output(
					concertId = 1L,
					availableSeats = listOf(
						ListSeatUseCase.Output.SeatInfo(seatId = 1L, seatNumber = 1, price = 100000, isAvailable = true),
						ListSeatUseCase.Output.SeatInfo(seatId = 2L, seatNumber = 2, price = 120000, isAvailable = true),
						ListSeatUseCase.Output.SeatInfo(seatId = 3L, seatNumber = 3, price = 150000, isAvailable = false)
					)
				)

				mockMvc.get("/api/v1/reservations/concerts/1/seats") {
					header("Queue-Token", "valid-token")
					accept = MediaType.APPLICATION_JSON
				}.andExpect {
					status { isOk() }
					jsonPath("$.code") { value("SUCCESS") }
				}
			}
		}
	}
})