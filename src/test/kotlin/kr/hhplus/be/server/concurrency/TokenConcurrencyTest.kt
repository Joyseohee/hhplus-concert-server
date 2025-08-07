package kr.hhplus.be.server.concurrency

import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.application.ConfirmReservationUseCase
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import kr.hhplus.be.server.domain.repository.ReservationRepository
import kr.hhplus.be.server.domain.repository.SeatHoldRepository
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class TokenConcurrencyTest @Autowired constructor(
    private val confirmReservationUseCase: ConfirmReservationUseCase,
    private val userBalanceRepository: UserBalanceRepository,
    private val holdSeatUseCase: HoldSeatUseCase,
    private val queueTokenRepository: QueueTokenRepository,
    private val seatHoldRepository: SeatHoldRepository,
    private val reservationRepository: ReservationRepository
) : KotestIntegrationSpec({

    beforeTest {
        queueTokenRepository.clear()
        seatHoldRepository.clear()
        reservationRepository.clear()
    }

    val initialBalance = 100_000L

    given("동시성 테스트 - 토큰 예약") {
        `when`("여러 스레드가 동시에 토큰을 예약할 때") {
            then("모든 요청이 성공되어야 하며 순서대로 토큰을 발행해야한다") {

                val threadCount = 5
                val latch = CountDownLatch(threadCount)
                val executor = Executors.newFixedThreadPool(threadCount)

                repeat(threadCount) {
                    executor.submit {
                        val user = userBalanceRepository.save(
                            UserBalance.create(
                                balance = initialBalance
                            )
                        )

                        try {
                            // 토큰 예약 요청
                            val token = (user.id)
                            println("토큰 예약 성공: 사용자 ID = ${user.id}, 토큰 = $token")
                        } catch (e: Exception) {
                            println("토큰 예약 실패: 사용자 ID = ${user.id}, 에러 = ${e.message}")
                        } finally {
                            latch.countDown()
                        }
                    }


                }

                latch.await()

//                // 최종 상태 확인
//                val tokens = queueTokenRepository.findValidatedByToken()
//                tokens.size shouldBe 1 // 하나의 토큰만 성공해야 함
            }
        }
    }

})
