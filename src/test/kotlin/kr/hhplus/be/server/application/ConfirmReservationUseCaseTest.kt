package kr.hhplus.be.server.application

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.model.Seat
import kr.hhplus.be.server.domain.model.SeatHold
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.*
import java.util.*

class ConfirmReservationUseCaseTest(
	private val confirmReservationUseCase: ConfirmReservationUseCase,
	private val seatRepository: SeatRepository,
	private val seatHoldRepository: SeatHoldRepository,
	private val reservationRepository: ReservationRepository,
	private val userBalanceRepository: UserBalanceRepository,
	private val queueTokenRepository: QueueTokenRepository
) : KotestIntegrationSpec({

	val CONCERT_ID = 1L
	val SEAT_NUMBER = 10
	val PRICE = 50_000L

	beforeEach {
		reservationRepository.clear()
		seatHoldRepository.clear()
		seatRepository.clear()
		userBalanceRepository.clear()
		queueTokenRepository.clear()
	}

	given("유효한 좌석 점유와 활성 토큰이 주어졌을 때") {
		`when`("예약을 요청하면") {
			then("예약이 확정되고 관련 데이터가 업데이트된다") {
				val user = userBalanceRepository.save(
					UserBalance.create(balance = PRICE + 1000L)
				)
				val seat = seatRepository.save(
					Seat.create(seatNumber = SEAT_NUMBER, price = PRICE)
				)
				val hold = seatHoldRepository.save(
					SeatHold.create(
						seatHoldUuid = UUID.randomUUID().toString(),
						userId = user.userId!!,
						concertId = CONCERT_ID,
						seatId = seat.seatId!!
					)
				)
				queueTokenRepository.save(
					QueueToken.create(userId = user.userId, status = QueueToken.Status.ACTIVE)
				)

				val output = confirmReservationUseCase.confirmReservation(
					hold.userId,
					ConfirmReservationUseCase.Input(
						reservationUuid = UUID.randomUUID().toString(),
						seatId = hold.seatId
					)
				)

				output.concertId shouldBe CONCERT_ID
				output.seatId shouldBe seat.seatId
				output.price shouldBe PRICE

				val savedRes = reservationRepository.findBySeatId(seat.seatId)!!
				savedRes.seatId shouldBe seat.seatId

				seatHoldRepository.findValidSeatHoldBySeatId(userId = user.userId, seatId = seat.seatId) shouldBe null

				queueTokenRepository.findByUserId(user.userId) shouldBe null
			}
		}
	}

	given("유효하지 않은 seatHold 상황에서") {
		`when`("seatHold가 없으면") {
			then("예외가 발생한다") {
				val user = userBalanceRepository.save(
					UserBalance.create(balance = PRICE)
				)
				val seat = seatRepository.save(
					Seat.create(seatNumber = SEAT_NUMBER, price = PRICE)
				)
				queueTokenRepository.save(
					QueueToken.create(userId = user.userId!!, status = QueueToken.Status.ACTIVE)
				)

				shouldThrowExactly<IllegalArgumentException> {
					confirmReservationUseCase.confirmReservation(
						user.userId,
						ConfirmReservationUseCase.Input(
							reservationUuid = UUID.randomUUID().toString(),
							seatId = seat.seatId!!
						)
					)
				}
			}
		}

		`when`("seatHold는 존재하나 seatRepository에 없으면") {
			then("예외가 발생한다") {
				val user = userBalanceRepository.save(
					UserBalance.create(balance = PRICE)
				)
				val seatHold = seatHoldRepository.save(
					SeatHold.create(
						seatHoldUuid = UUID.randomUUID().toString(),
						userId = user.userId!!,
						concertId = CONCERT_ID,
						seatId = 12345L
					)
				)
				queueTokenRepository.save(
					QueueToken.create(userId = user.userId, status = QueueToken.Status.ACTIVE)
				)

				shouldThrowExactly<IllegalArgumentException> {
					confirmReservationUseCase.confirmReservation(
						user.userId,
						ConfirmReservationUseCase.Input(
							reservationUuid = UUID.randomUUID().toString(),
							seatId = seatHold.seatId
						)
					)
				}
			}
		}
	}

	given("사용자 정보나 토큰이 없을 때") {
		`when`("userBalance가 없으면") {
			then("예외가 발생한다") {
				val seat = seatRepository.save(
					Seat.create(seatNumber = SEAT_NUMBER, price = PRICE)
				)
				val hold = seatHoldRepository.save(
					SeatHold.create(
						seatHoldUuid = UUID.randomUUID().toString(),
						userId = 999L,
						concertId = CONCERT_ID,
						seatId = seat.seatId!!
					)
				)

				shouldThrowExactly<IllegalArgumentException> {
					confirmReservationUseCase.confirmReservation(
						999L,
						ConfirmReservationUseCase.Input(
							reservationUuid = UUID.randomUUID().toString(),
							seatId = seat.seatId
						)
					)
				}
			}
		}

		`when`("QueueToken이 없으면") {
			then("예외가 발생한다") {
				val user = userBalanceRepository.save(
					UserBalance.create(balance = PRICE)
				)
				val seat = seatRepository.save(
					Seat.create(seatNumber = SEAT_NUMBER, price = PRICE)
				)
				val hold = seatHoldRepository.save(
					SeatHold.create(
						seatHoldUuid = UUID.randomUUID().toString(),
						userId = user.userId!!,
						concertId = CONCERT_ID,
						seatId = seat.seatId!!
					)
				)

				shouldThrowExactly<IllegalArgumentException> {
					confirmReservationUseCase.confirmReservation(
						user.userId,
						ConfirmReservationUseCase.Input(
							reservationUuid = UUID.randomUUID().toString(),
							seatId = seat.seatId!!
						)
					)
				}
			}
		}
	}
})
