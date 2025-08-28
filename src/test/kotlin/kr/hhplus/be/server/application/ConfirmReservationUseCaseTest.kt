package kr.hhplus.be.server.application

import org.awaitility.kotlin.await
import org.awaitility.kotlin.until

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.application.event.listener.AggregatePopularConcertEventHandler
import kr.hhplus.be.server.application.event.listener.ExpireQueueTokenEventHandler
import kr.hhplus.be.server.application.event.listener.SendReservationDataEventHandler
import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.model.Seat
import kr.hhplus.be.server.domain.model.SeatHold
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.*
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import java.util.*

@Import(
    MockConfig::class
)
class ConfirmReservationUseCaseTest(
    private val confirmReservationUseCase: ConfirmReservationUseCase,
    private val seatRepository: SeatRepository,
    private val seatHoldRepository: SeatHoldRepository,
    private val reservationRepository: ReservationRepository,
    private val userBalanceRepository: UserBalanceRepository,
    private val queueTokenRepository: QueueTokenRepository,
    private val concertAggregationRepository: ConcertAggregationRepository,
    private val aggregatePopularConcertUseCase: AggregatePopularConcertUseCase,
    private val expireQueueTokenUseCase: ExpireQueueTokenUseCase,
    private val sendReservationDataUseCase: SendReservationDataUseCase,
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
        concertAggregationRepository.clear("popular:concerts")
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
                    SeatHold.held(
                        seatHoldUuid = UUID.randomUUID().toString(),
                        userId = user.userId!!,
                        concertId = CONCERT_ID,
                        seatId = seat.seatId!!
                    )
                )

                val savedToken = queueTokenRepository.save(
                    QueueToken.create(userId = user.userId, status = QueueToken.Status.ACTIVE)
                )

                val output = confirmReservationUseCase.confirmReservation(
                    hold.userId,
                    ConfirmReservationUseCase.Input(
                        reservationUuid = UUID.randomUUID().toString(),
                        seatHoldUuid = hold.seatHoldUuid
                    )
                )

                output.concertId shouldBe CONCERT_ID
                output.seatId shouldBe seat.seatId
                output.price shouldBe PRICE

                val savedRes = reservationRepository.findBySeatId(seat.seatId)!!
                savedRes.seatId shouldBe seat.seatId

                seatHoldRepository.findByUserIdAndUuid(userId = user.userId, seatHoldUuid = hold.seatHoldUuid) shouldNotBe null

                userBalanceRepository.findById(user.userId)?.let {
                    it.balance shouldBe (1000L)
                }
            }

            then("예약이 확정되고 이벤트가 처리된다") {
                val user = userBalanceRepository.save(
                    UserBalance.create(balance = PRICE + 1000L)
                )
                val seat = seatRepository.save(
                    Seat.create(seatNumber = SEAT_NUMBER, price = PRICE)
                )
                val hold = seatHoldRepository.save(
                    SeatHold.held(
                        seatHoldUuid = UUID.randomUUID().toString(),
                        userId = user.userId!!,
                        concertId = CONCERT_ID,
                        seatId = seat.seatId!!
                    )
                )

                val savedToken = queueTokenRepository.save(
                    QueueToken.create(userId = user.userId, status = QueueToken.Status.ACTIVE)
                )

                AggregatePopularConcertEventHandler(aggregatePopularConcertUseCase)
                ExpireQueueTokenEventHandler(expireQueueTokenUseCase)
                SendReservationDataEventHandler(sendReservationDataUseCase)

                val reservationUuid = UUID.randomUUID().toString()
                val output = confirmReservationUseCase.confirmReservation(
                    hold.userId,
                    ConfirmReservationUseCase.Input(
                        reservationUuid = reservationUuid,
                        seatHoldUuid = hold.seatHoldUuid
                    )
                )

                val reservation = reservationRepository.findByUuid(reservationUuid)

                await until {
                    verify(exactly = 1) { aggregatePopularConcertUseCase.execute(reservation!!.reservationId!!) }
                    verify(exactly = 1) { expireQueueTokenUseCase.execute(user.userId) }
                    verify(exactly = 1) { sendReservationDataUseCase.execute(reservation!!.reservationId!!, user.userId) }
                    true
                }

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
                            seatHoldUuid = UUID.randomUUID().toString()
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
                            seatHoldUuid = seatHold.seatHoldUuid
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
                            seatHoldUuid = hold.seatHoldUuid
                        )
                    )
                }
            }
        }
    }

})


@TestConfiguration
class MockConfig {
    @Bean
    fun aggregatePopularConcertUseCase() = mockk<AggregatePopularConcertUseCase>(relaxed = true)
    @Bean
    fun expireQueueTokenUseCase() = mockk<ExpireQueueTokenUseCase>(relaxed = true)
    @Bean
    fun sendReservationDataUseCase() = mockk<SendReservationDataUseCase>(relaxed = true)
}
