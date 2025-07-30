package kr.hhplus.be.server.application

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.model.Seat
import kr.hhplus.be.server.domain.repository.SeatHoldRepository
import kr.hhplus.be.server.domain.repository.SeatRepository
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.util.UUID

@SpringBootTest
class HoldSeatUseCaseTest @Autowired constructor(
    val holdSeatUseCase: HoldSeatUseCase,
    val userBalanceRepository: UserBalanceRepository,
    val seatRepository: SeatRepository,
    val seatHoldRepository: SeatHoldRepository,
) : BehaviorSpec({
    val validUserId = 1L
    val invalidUserId = 2L
    val seatId = 1L
    val request = HoldSeatUseCase.Input(
        seatHoldUuid = UUID.randomUUID().toString(),
        concertId = 1L,
        seatId = seatId
    )

    beforeTest {
        // 테스트를 위한 초기화 로직
        userBalanceRepository.clear()
        seatRepository.clear()
        seatHoldRepository.clear()

        userBalanceRepository.save(
            UserBalance.create(userId = validUserId, balance = 50_000L)
        )

        for (i in 1..10) {
            seatRepository.save(
                Seat.create(
                    seatNumber = i,
                    price = 130_000L
                )
            )
        }
        seatHoldRepository
    }

    given("좌석 점유를 요청할 때") {
        `when`("점유되지 않은 좌석을 유효한 사용자가 요청할 때") {
            then("점유가 성공적으로 이루어져야 한다") {
                val result = holdSeatUseCase.holdSeat(userId = validUserId, input = request)
                result.seatHoldUuid shouldBe request.seatHoldUuid
                result.seatId shouldBe request.seatId
                result.expiresAt.isAfter(Instant.now()) shouldBe true
            }
        }
        `when`("이미 점유된 좌석을 점유하려고 할 때") {
            then("예외가 발생해야 한다") {
                shouldThrowExactly<IllegalArgumentException> {
                    holdSeatUseCase.holdSeat(userId = validUserId, input = request)
                    holdSeatUseCase.holdSeat(
                        userId = invalidUserId, input = HoldSeatUseCase.Input(
                            seatHoldUuid = UUID.randomUUID().toString(),
                            concertId = request.concertId,
                            seatId = request.seatId
                        )
                    )
                }
            }
        }
    }
})
