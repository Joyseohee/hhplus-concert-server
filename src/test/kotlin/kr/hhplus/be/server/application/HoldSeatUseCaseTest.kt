package kr.hhplus.be.server.application

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.model.*
import kr.hhplus.be.server.domain.repository.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class HoldSeatUseCaseTest(
    private val holdSeatUseCase: HoldSeatUseCase,
    private val seatRepository: SeatRepository,
    private val reservationRepository: ReservationRepository,
    private val seatHoldRepository: SeatHoldRepository
) : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    companion object {
        private const val CONCERT_ID = 1L
        private const val SEAT_NUMBER = 5
        private const val PRICE = 20_000L
    }

    init {
        afterEach {
            reservationRepository.clear()
            seatHoldRepository.clear()
            seatRepository.clear()
        }

        given("좌석 점유 요청을 할 때") {
            `when`("유효한 좌석이고 아직 예약되지 않았다면") {
                then("정상적으로 점유가 생성된다") {
                    val seat = seatRepository.save(
                        Seat.create(seatNumber = SEAT_NUMBER, price = PRICE)
                    )
                    val uuid = UUID.randomUUID().toString()

                    val output = holdSeatUseCase.holdSeat(
                        HoldSeatUseCase.Input(
                            seatHoldUuid = uuid,
                            concertId = CONCERT_ID,
                            seatId = seat.seatId!!
                        ),
                        userId = 123L
                    )

                    output.seatHoldUuid shouldBe uuid
                    output.seatId       shouldBe seat.seatId
                    output.expiresAt     shouldBe seatHoldRepository
                        .findByUuid(uuid)!!.expiresAt

                    val persisted = seatHoldRepository.findByUuid(uuid)!!
                    persisted.seatHoldUuid shouldBe uuid
                    persisted.seatId       shouldBe seat.seatId
                }
            }

            `when`("존재하지 않는 좌석 ID로 요청하면") {
                then("예외가 발생한다") {
                    val badId = 9999L
                    shouldThrowExactly<IllegalArgumentException> {
                        holdSeatUseCase.holdSeat(
                            HoldSeatUseCase.Input(
                                seatHoldUuid = UUID.randomUUID().toString(),
                                concertId = CONCERT_ID,
                                seatId = badId
                            ),
                            userId = 1L
                        )
                    }
                }
            }

            `when`("이미 예약된 좌석에 대해 요청하면") {
                then("예외가 발생한다") {
                    val seat = seatRepository.save(
                        Seat.create(seatNumber = SEAT_NUMBER, price = PRICE)
                    )
                    reservationRepository.save(
                        Reservation.create(
                            reservationUuid = UUID.randomUUID().toString(),
                            userId = 999L,
                            concertId = CONCERT_ID,
                            seatId = seat.seatId!!,
                            reservedAt = Instant.now(),
                            price = PRICE
                        )
                    )

                    shouldThrowExactly<IllegalArgumentException> {
                        holdSeatUseCase.holdSeat(
                            HoldSeatUseCase.Input(
                                seatHoldUuid = UUID.randomUUID().toString(),
                                concertId = CONCERT_ID,
                                seatId = seat.seatId
                            ),
                            userId = 1L
                        )
                    }
                }
            }
        }
    }
}
