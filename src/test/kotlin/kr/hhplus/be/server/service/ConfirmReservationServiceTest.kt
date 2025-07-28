package kr.hhplus.be.server.service

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.*
import kr.hhplus.be.server.domain.SeatHold.Companion.VALID_HOLD_MINUTE
import kr.hhplus.be.server.support.error.SeatHoldUnavailableException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.util.*

@SpringBootTest
class ConfirmReservationServiceTest @Autowired constructor(
	private val confirmReservationService: ConfirmReservationService,
	private val seatRepository: SeatRepository,
	private val seatHoldRepository: SeatHoldRepository,
	private val reservationRepository: ReservationRepository,
	private val userBalanceRepository: UserBalanceRepository
) : BehaviorSpec({
	val userId = 1L
	val concertId = 1L
	val seatId = 1L
	val seatNumber = 1
	val price = 10_000L
	val seatHoldUuid = UUID.randomUUID().toString()
	val reservationUuid = UUID.randomUUID().toString()

	beforeTest {
		userBalanceRepository.clear()
		seatRepository.clear()
		seatHoldRepository.clear()
		reservationRepository.clear()
	}

	given("점유한 좌석에 대해 예약을 요청할 때") {
		`when`("유효한 사용자가 점유해둔 좌석을 예약한다면") {
			then("예약에 성공하고 예약 정보가 반환되어야 한다") {
				userBalanceRepository.save(UserBalance.create(userId = userId, balance = price + 1_000L))
				seatRepository.save(Seat.create(seatId = seatId, seatNumber = seatNumber, price = price))
				seatHoldRepository.save(SeatHold.create(seatHoldUuid = seatHoldUuid, userId = userId, concertId = concertId, seatId = seatId))

				val result = confirmReservationService.confirmReservation(
					userId, ConfirmReservationService.Input(
						reservationUuid = UUID.randomUUID().toString(),
						seatId = seatId
					)
				)

				result shouldBe ConfirmReservationService.Output(
					concertId = concertId,
					seatId = seatId,
					price = price
				)
			}
		}
		`when`("점유한 적 없는 좌석에 대해 예약을 시도할 때") {
			then("예외가 발생해야 한다") {
				userBalanceRepository.save(UserBalance.create(userId = userId, balance = price + 1_000L))
				seatRepository.save(Seat.create(seatId = seatId, seatNumber = seatNumber, price = price))

				shouldThrowExactly<IllegalArgumentException> {
					confirmReservationService.confirmReservation(
						userId, ConfirmReservationService.Input(
							reservationUuid = reservationUuid,
							seatId = seatId
						)
					)
				}.message shouldBe "유효하지 않은 좌석 예약입니다."
			}
		}
		`when`("점유가 만료된 좌석에 대해 예약을 시도할 때") {
			then("예외가 발생해야 한다") {
				val expiresAt = Instant.now().minusSeconds(VALID_HOLD_MINUTE * 60)

				userBalanceRepository.save(UserBalance.create(userId = userId, balance = price + 1_000L))
				seatRepository.save(Seat.create(seatId = seatId, seatNumber = seatNumber, price = price))
				seatHoldRepository.save(SeatHold.create(seatHoldUuid = seatHoldUuid, userId = userId, concertId = concertId, seatId = seatId, expiresAt = expiresAt))

				shouldThrowExactly<SeatHoldUnavailableException> {
					confirmReservationService.confirmReservation(
						userId, ConfirmReservationService.Input(
							reservationUuid = reservationUuid,
							seatId = seatId
						)
					)
				}
			}
		}
		`when`("동일한 사용자가 여러번 예약을 시도할 때") {
			then("예외가 발생해야 한다") {
				userBalanceRepository.save(UserBalance.create(userId = userId, balance = price + 1_000L))
				seatRepository.save(Seat.create(seatId = seatId, seatNumber = 1, price = price))
				seatHoldRepository.save(SeatHold.create(seatHoldUuid = seatHoldUuid, userId = userId, concertId = concertId, seatId = seatId))
				reservationRepository.save(Reservation.create(reservationUuid = reservationUuid, userId = userId, concertId = concertId, seatId = seatId, reservedAt = Instant.now(), price = price))

				shouldThrowExactly<IllegalArgumentException> {
					confirmReservationService.confirmReservation(
						userId, ConfirmReservationService.Input(
							reservationUuid = reservationUuid,
							seatId = seatId
						)
					)
				}.message shouldBe "중복된 요청입니다."
			}
		}
		`when`("좌석 가격에 비해 잔고가 부족할 때") {
			then("예외가 발생해야 한다") {
				userBalanceRepository.save(UserBalance.create(userId = userId, balance = price - 1_000L))
				seatRepository.save(Seat.create(seatId = seatId, seatNumber = 1, price = price))
				seatHoldRepository.save(SeatHold.create(seatHoldUuid = seatHoldUuid, userId = userId, concertId = concertId, seatId = seatId))

				shouldThrowExactly<IllegalArgumentException> {
					confirmReservationService.confirmReservation(
						userId,
						ConfirmReservationService.Input(
							reservationUuid = reservationUuid,
							seatId = seatId
						)
					)
				}
			}
		}
		`when`("다른 사용자가 점유한 좌석을 예약하려고 할 때") {
			then("예외가 발생해야 한다") {
				userBalanceRepository.save(UserBalance.create(userId = userId, balance = price + 1_000L))
				userBalanceRepository.save(UserBalance.create(userId = 2L, balance = price + 1_000L))
				seatRepository.save(Seat.create(seatId = seatId, seatNumber = seatNumber, price = price))
				seatHoldRepository.save(SeatHold.create(seatHoldUuid = seatHoldUuid, userId = userId, concertId = concertId, seatId = seatId))
				reservationRepository.save(Reservation.create(reservationUuid = reservationUuid, userId = userId, concertId = concertId, seatId = seatId, reservedAt = Instant.now(), price = price))

				shouldThrowExactly<SeatHoldUnavailableException> {
					confirmReservationService.confirmReservation(
						2L,
						ConfirmReservationService.Input(
							reservationUuid = seatHoldUuid,
							seatId = seatId
						)
					)
				}
			}
		}
	}
})
