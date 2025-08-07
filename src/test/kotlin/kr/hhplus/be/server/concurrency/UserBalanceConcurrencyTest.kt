package kr.hhplus.be.server.concurrency

import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.application.ChargeBalanceUseCase
import kr.hhplus.be.server.application.ConfirmReservationUseCase
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class UserBalanceConcurrencyTest @Autowired constructor(
    private val userBalanceRepository: UserBalanceRepository,
    private val chargeBalanceUseCase: ChargeBalanceUseCase,
    private val reservationUseCase: ConfirmReservationUseCase
) : KotestIntegrationSpec( {
    beforeTest {
        userBalanceRepository.clear()
    }

    val amount = 1_000L
    val initialBalance = 100_000L

    given("동시성 테스트 - 잔액 충전") {
        `when`("여러 스레드가 동시에 잔액을 충전할 때") {
            then("최종 잔액이 올바르게 계산되어야 한다") {
                // val chargeAmount = 1_000L
                // val initialBalance = 100_000L
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
                            reservationUseCase.confirmReservation(
                                userId = user.userId!!,
                                input = ConfirmReservationUseCase.Input(
                                    reservationUuid = UUID.randomUUID().toString(),
                                    seatId = user.userId!!,
                                )
                            )
                        } finally {
                            latch.countDown()
                        }
                    }
                }
                latch.await()

                val finalBalance = userBalanceRepository.findById(user.userId!!)?.balance ?: 0

                finalBalance shouldBe initialBalance - amount * threadCount // 100_000 - 1_000 * 2

            }
        }
    }

    // todo - 마지막 테스트는 충전과 차감을 동시에 진행하는 테스트로, 충전과 차감이 동시에 이루어질 때 최종 잔액이 올바르게 계산되는지 확인하는 테스트입니다.
    given("동시성 테스트 - 잔액 충전 및 차감") {
        `when`("여러 스레드가 동시에 잔액을 충전과 차감을 요청할 때") {
            then("최종 잔액이 올바르게 계산되어야 한다") {
                val user = userBalanceRepository.save(
                    UserBalance.create(
                        balance = initialBalance
                    )
                )

                val threadCount = 2
                val latch = CountDownLatch(threadCount * 2) // 충전과 차감 각각 2개씩
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

                    executor.submit {
                        try {
                            reservationUseCase.confirmReservation(
                                userId = user.userId!!,
                                input = ConfirmReservationUseCase.Input(
                                    reservationUuid = UUID.randomUUID().toString(),
                                    seatId = user.userId!!,
                                )
                            )
                        } finally {
                            latch.countDown()
                        }
                    }
                }
                latch.await()

                val finalBalance = userBalanceRepository.findById(user.userId!!)?.balance ?: 0

                finalBalance shouldBe initialBalance // 충전과 차감이 동일하게 이루어지므로 최종 잔액은 초기 잔액과 같아야 함
            }
        }
    }

})
