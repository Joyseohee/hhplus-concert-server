package kr.hhplus.be.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.ConfirmReservationUseCase
import kr.hhplus.be.server.application.HoldSeatUseCase
import kr.hhplus.be.server.application.ListConcertUseCase
import kr.hhplus.be.server.application.ListPopularConcertUseCase
import kr.hhplus.be.server.application.ListSeatUseCase
import kr.hhplus.be.server.application.validation.ValidateQueueTokenService
import kr.hhplus.be.server.application.validation.ValidateUserService
import kr.hhplus.be.server.config.WebConfig
import kr.hhplus.be.server.presentation.controller.ReservationController
import kr.hhplus.be.server.support.error.GlobalExceptionHandler
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant

@WebMvcTest(ReservationController::class)
@Import(
	WebConfig::class,
	GlobalExceptionHandler::class,
	ReservationControllerTest.ReserveMockConfig::class
)
@ActiveProfiles("test")
class ReservationControllerTest(
	private val mockMvc: MockMvc,
	private val objectMapper: ObjectMapper,
	private val holdSeatUseCase: HoldSeatUseCase,
	private val confirmReservationUseCase: ConfirmReservationUseCase,
	private val concertUseCase: ListConcertUseCase,
	private val listPopularConcertUseCase: ListPopularConcertUseCase,
	private val seatUseCase: ListSeatUseCase,
	private val validateQueueTokenService: ValidateQueueTokenService,
	private val validateUserService: ValidateUserService
) : BehaviorSpec() {

	override fun extensions() = listOf(SpringExtension)

	companion object {
		private const val QUEUE_TOKEN_HEADER = "Queue-Token"
		private const val VALID_USER_ID = 1L
		private const val VALID_TOKEN = "valid-token"
		private val EXPIRES_AT = Instant.parse("2025-07-20T19:12:34Z")
	}

	init {
		beforeEach {
			every { validateQueueTokenService.validateToken(VALID_TOKEN) } returns VALID_USER_ID
			every { validateUserService.validateUser(VALID_USER_ID) } returns Unit
		}

		given("좌석 점유 요청 API POST /api/v1/reservations/concerts/{concertId}/seats/hold") {
			`when`("유효한 요청 바디와 헤더가 주어지면") {
				then("200 OK, SUCCESS 반환") {
					val req = HoldSeatUseCase.Input(
						seatHoldUuid = "uuid",
						concertId = 1L,
						seatId = 1L
					)
					every {
						holdSeatUseCase.holdSeat(userId = VALID_USER_ID, input = req)
					} returns HoldSeatUseCase.Output(
						seatHoldUuid = "uuid",
						seatId = 1,
						expiresAt = EXPIRES_AT
					)

					mockMvc.post("/api/v1/reservations/concerts/1/seats/hold") {
						header(QUEUE_TOKEN_HEADER, VALID_TOKEN)
						contentType = MediaType.APPLICATION_JSON
						content = objectMapper.writeValueAsString(req)
					}.andExpect {
						status { isOk() }
						jsonPath("$.code") { value("SUCCESS") }
					}
				}
			}
			`when`("잘못된 요청 바디가 주어지면") {
				then("400 Bad Request 반환") {
					mockMvc.post("/api/v1/reservations/concerts/1/seats/hold") {
						header(QUEUE_TOKEN_HEADER, VALID_TOKEN)
						contentType = MediaType.APPLICATION_JSON
						content = "{}"
					}.andExpect {
						status { isBadRequest() }
					}
				}
			}
		}

		given("예약 확정 요청 API POST /api/v1/reservations") {
			`when`("유효한 요청 바디와 헤더가 주어지면") {
				then("200 OK, SUCCESS 반환") {
					val req = ConfirmReservationUseCase.Input(
						reservationUuid = "res-uuid",
						seatHoldUuid = "hold-uuid"
					)
					every {
						confirmReservationUseCase.confirmReservation(VALID_USER_ID, req)
					} returns ConfirmReservationUseCase.Output(
						concertId = 1L,
						seatId = 1L,
						price = 130000
					)

					mockMvc.post("/api/v1/reservations") {
						header(QUEUE_TOKEN_HEADER, VALID_TOKEN)
						contentType = MediaType.APPLICATION_JSON
						content = objectMapper.writeValueAsString(req)
					}.andExpect {
						status { isOk() }
						jsonPath("$.code") { value("SUCCESS") }
					}
				}
			}
			`when`("잘못된 요청 바디가 주어지면") {
				then("400 Bad Request 반환") {
					mockMvc.post("/api/v1/reservations") {
						header(QUEUE_TOKEN_HEADER, VALID_TOKEN)
						contentType = MediaType.APPLICATION_JSON
						content = "{}"
					}.andExpect {
						status { isBadRequest() }
					}
				}
			}
		}

		given("콘서트 목록 조회 API GET /api/v1/reservations/concerts") {
			`when`("헤더만 주어지면") {
				then("200 OK, SUCCESS 반환") {
					every { concertUseCase.listConcerts() } returns ListConcertUseCase.Output(
						listOf(
							ListConcertUseCase.Output.ConcertInfo(
								concertId = 1L,
								concertTitle = "Test Concert",
								concertVenue = "Test Venue",
								concertDateTime = EXPIRES_AT.toString(),
								isAvailable = true
							)
						)
					)

					mockMvc.get("/api/v1/reservations/concerts") {
						header(QUEUE_TOKEN_HEADER, VALID_TOKEN)
					}.andExpect {
						status { isOk() }
						jsonPath("$.code") { value("SUCCESS") }
					}
				}
			}
		}

		given("좌석 목록 조회 API GET /api/v1/reservations/concerts/{concertId}/seats") {
			`when`("유효한 콘서트 ID와 헤더가 주어지면") {
				then("200 OK, SUCCESS 반환") {
					every {
						seatUseCase.listAvailableSeats(concertId = 1L, userId = VALID_USER_ID)
					} returns ListSeatUseCase.Output(
						concertId = 1L,
						availableSeats = listOf(
							ListSeatUseCase.Output.SeatInfo(1L, 1, 100000, true),
							ListSeatUseCase.Output.SeatInfo(2L, 2, 120000, true),
							ListSeatUseCase.Output.SeatInfo(3L, 3, 150000, false)
						)
					)

					mockMvc.get("/api/v1/reservations/concerts/1/seats") {
						header(QUEUE_TOKEN_HEADER, VALID_TOKEN)
					}.andExpect {
						status { isOk() }
						jsonPath("$.code") { value("SUCCESS") }
					}
				}
			}
		}

		given("인기 콘서트 목록 조회 API GET /api/v1/reservations/concerts/popular") {
			`when`("유효한 토큰이 주어지면") {
				then("200 OK, SUCCESS 반환") {
					every {
						listPopularConcertUseCase.listPopularConcert()
					} returns ListPopularConcertUseCase.Output(
						popularConcert = listOf(
							ListPopularConcertUseCase.Output.ConcertInfo(
								rank = 1,
								concertId = 1L,
								concertTitle = "Test Concert",
								concertVenue = "Test Venue",
								concertDateTime = EXPIRES_AT.toString(),
								isAvailable = true
							),
							ListPopularConcertUseCase.Output.ConcertInfo(
								rank = 2,
								concertId = 2L,
								concertTitle = "Test Concert",
								concertVenue = "Test Venue",
								concertDateTime = EXPIRES_AT.toString(),
								isAvailable = true
							),
							ListPopularConcertUseCase.Output.ConcertInfo(
								rank = 3,
								concertId = 3L,
								concertTitle = "Test Concert",
								concertVenue = "Test Venue",
								concertDateTime = EXPIRES_AT.toString(),
								isAvailable = true
							),
						)
					)

					mockMvc.get("/api/v1/reservations/concerts/popular") {
						header(QUEUE_TOKEN_HEADER, VALID_TOKEN)
					}.andExpect {
						status { isOk() }
						jsonPath("$.code") { value("SUCCESS") }
					}
				}
			}
		}
	}

	@TestConfiguration
	class ReserveMockConfig {
		@Bean
		fun holdSeatUseCase() = mockk<HoldSeatUseCase>(relaxed = true)
		@Bean
		fun confirmReservationUseCase() = mockk<ConfirmReservationUseCase>(relaxed = true)
		@Bean
		fun concertUseCase() = mockk<ListConcertUseCase>(relaxed = true)
		@Bean
		fun listPopularConcertUseCase() = mockk<ListPopularConcertUseCase>(relaxed = true)
		@Bean
		fun seatUseCase() = mockk<ListSeatUseCase>(relaxed = true)
		@Bean
		fun validateQueueTokenService() = mockk<ValidateQueueTokenService>(relaxed = true)
		@Bean
		fun validateUserService() = mockk<ValidateUserService>(relaxed = true)
	}
}
