package kr.hhplus.be.server.concurrency

import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.application.ChargeBalanceUseCase
import kr.hhplus.be.server.application.ConfirmReservationUseCase
import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.model.Seat
import kr.hhplus.be.server.domain.model.SeatHold
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.*
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class UserBalanceConcurrencyTest @Autowired constructor(
    private val userBalanceRepository: UserBalanceRepository,
    private val seatRepository: SeatRepository,
    private val seatHoldRepository: SeatHoldRepository,
    private val reservationRepository: ReservationRepository,
    private val queueTokenRepository: QueueTokenRepository,
    private val chargeBalanceUseCase: ChargeBalanceUseCase,
    private val reservationUseCase: ConfirmReservationUseCase
) : KotestIntegrationSpec({

    beforeEach {
        userBalanceRepository.clear()
        seatRepository.clear()
        seatHoldRepository.clear()
        reservationRepository.clear()
        queueTokenRepository.clear()
    }

    val amount = 1_000L
    val initialBalance = 100_000L

    given("동시성 테스트 - 잔액 충전") {
        `when`("여러 스레드가 동시에 잔액을 충전할 때") {
            then("최종 잔액이 올바르게 계산되어야 한다") {
                val user = userBalanceRepository.save(
                    UserBalance.create(
                        balance = initialBalance
                    )
                )

                val threadCount = 2
                val latch = CountDownLatch(threadCount)
                val executor = Executors.newFixedThreadPool(threadCount)

                repeat(threadCount) {
                    executor.submit {
                        try {
                            chargeBalanceUseCase.chargeBalance(
                                userId = user.userId!!,
                                input = ChargeBalanceUseCase.Input(amount = amount)
                            )
                        } finally {
                            latch.countDown()
                        }
                    }
                }
                latch.await()

                val finalBalance = userBalanceRepository.findById(user.userId!!)?.balance ?: 0

                finalBalance shouldBe initialBalance + amount * threadCount
            }
        }
    }

    given("동시성 테스트 - 잔액 차감") {
        `when`("여러 스레드가 동시에 잔액을 차감할 때") {
            then("토큰이 만료되므로 한 개의 요청만 처리된다") {
                val user = userBalanceRepository.save(
                    UserBalance.create(
                        balance = initialBalance
                    )
                )

                val seats = listOf(
                    Seat.create(seatNumber = 1, price = amount),
                    Seat.create(seatNumber = 2, price = amount)
                ).map { seatRepository.save(it) }

                queueTokenRepository.save(
                    QueueToken.create(
                        userId = user.userId!!
                    )
                )

                val threadCount = 2
                val latch = CountDownLatch(threadCount)
                val executor = Executors.newFixedThreadPool(threadCount)

                executor.submit {
                    try {
                        val hold = seatHoldRepository.save(
                            // 좌석 점유 요청을 생성합니다.
                            SeatHold.create(
                                seatHoldUuid = UUID.randomUUID().toString(),
                                userId = user.userId,
                                concertId = 1L, // 임의의 콘서트 ID
                                seatId = seats.get(0).seatId!!,
                            )
                        )

                        reservationUseCase.confirmReservation(
                            userId = user.userId,
                            input = ConfirmReservationUseCase.Input(
                                reservationUuid = UUID.randomUUID().toString(),
                                seatHoldUuid = hold.seatHoldUuid
                            )
                        )
                    } finally {
                        latch.countDown()
                    }
                }
                executor.submit {
                    try {
                        val hold = seatHoldRepository.save(
                            // 좌석 점유 요청을 생성합니다.
                            SeatHold.create(
                                seatHoldUuid = UUID.randomUUID().toString(),
                                userId = user.userId,
                                concertId = 1L, // 임의의 콘서트 ID
                                seatId = seats.get(1).seatId!!,
                            )
                        )

                        reservationUseCase.confirmReservation(
                            userId = user.userId,
                            input = ConfirmReservationUseCase.Input(
                                reservationUuid = UUID.randomUUID().toString(),
                                seatHoldUuid = hold.seatHoldUuid
                            )
                        )
                    } finally {
                        latch.countDown()
                    }
                }
                latch.await()

                val finalBalance = userBalanceRepository.findById(user.userId)?.balance ?: 0
                println("최종 잔액: $finalBalance")

                finalBalance shouldBe initialBalance - amount // 100_000 - 1_000
            }
        }
    }

    // 충전과 차감을 동시에 진행하는 테스트로, 충전과 차감이 동시에 이루어질 때 최종 잔액이 올바르게 계산되는지 확인하는 테스트입니다.
    given("동시성 테스트 - 잔액 충전 및 차감") {
        `when`("여러 스레드가 동시에 잔액을 충전과 차감을 요청할 때") {
            then("충전은 모두 성공해야하고 사용은 한 번만 성공해야 한다") {
                val user = userBalanceRepository.save(
                    UserBalance.create(
                        balance = initialBalance
                    )
                )

                val seats = listOf(
                    Seat.create(seatNumber = 1, price = amount),
                    Seat.create(seatNumber = 2, price = amount)
                ).map { seatRepository.save(it) }

                queueTokenRepository.save(
                    QueueToken.create(
                        userId = user.userId!!
                    )
                )

                val hold = seatHoldRepository.save(
                    // 좌석 점유 요청을 생성합니다.
                    SeatHold.create(
                        seatHoldUuid = UUID.randomUUID().toString(),
                        userId = user.userId,
                        concertId = 1L, // 임의의 콘서트 ID
                        seatId = seats.get(0).seatId!!,
                    )
                )

                val threadCount = 2
                val latch = CountDownLatch(threadCount * 2) // 충전과 차감 각각 2개씩
                val executor = Executors.newFixedThreadPool(threadCount)

                var i = 0

                repeat(threadCount) {
                    executor.submit {
                        try {
                            chargeBalanceUseCase.chargeBalance(
                                userId = user.userId,
                                input = ChargeBalanceUseCase.Input(amount = amount)
                            )
                        } finally {
                            latch.countDown()
                        }
                    }

                    executor.submit {
                        try {
                            reservationUseCase.confirmReservation(
                                userId = user.userId,
                                input = ConfirmReservationUseCase.Input(
                                    reservationUuid = UUID.randomUUID().toString(),
                                    seatHoldUuid = hold.seatHoldUuid
                                )
                            )
                        } finally {
                            latch.countDown()
                            i++
                        }
                    }
                }
                latch.await()

                val finalBalance = userBalanceRepository.findById(user.userId)?.balance ?: 0

                finalBalance shouldBe initialBalance + amount // 충전은 두 번 모두 정상적으로 이뤄지지만 결제는 좌석 당 한 번만 가능하다.
            }
        }
    }

})
