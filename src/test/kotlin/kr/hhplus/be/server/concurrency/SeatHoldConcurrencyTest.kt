package kr.hhplus.be.server.concurrency

import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.application.ConfirmReservationUseCase
import kr.hhplus.be.server.application.HoldSeatUseCase
import kr.hhplus.be.server.domain.model.Concert
import kr.hhplus.be.server.domain.model.Seat
import kr.hhplus.be.server.domain.repository.ConcertRepository
import kr.hhplus.be.server.domain.repository.SeatHoldRepository
import kr.hhplus.be.server.domain.repository.SeatRepository
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class SeatHoldConcurrencyTest @Autowired constructor(
	private val holdSeatUseCase: HoldSeatUseCase,
	private val seatHoldRepository: SeatHoldRepository,
	private val concertRepository: ConcertRepository,
	private val seatRepository: SeatRepository
) : KotestIntegrationSpec({

	beforeTest {
		// 초기화 작업이 필요하다면 여기에 작성
		seatHoldRepository.clear()
		concertRepository.clear()
		seatRepository.clear()
	}

	val userId = 1L
	val seatCount = 10

	given("동시성 테스트 - 좌석 예약") {
		`when`("여러 스레드가 동시에 하나의 좌석을 예약할 때") {
			then("하나의 요청만 성공해야 한다") {
				val concert = concertRepository.save(
					Concert.create(
						title = "콘서트",
						venue = "올림픽공원",
						showDateTime = Instant.now().plusSeconds(3600 * 24 * 7), // 1주일 후
					)
				)

				val seats = (1..seatCount).map { seatNum ->
					seatRepository.save(
						Seat.create(
							seatNumber = seatNum,
							price = 10000L
						)
					)
				}

				val threadCount = 5
				val latch = CountDownLatch(threadCount)
				val executor = Executors.newFixedThreadPool(threadCount)

				val userIds = listOf(userId, userId + 1, userId + 2, userId + 3, userId + 4)

				repeat(threadCount) {
					executor.submit {
						try {
							holdSeatUseCase.holdSeat(
								userId = userIds[it],
								input = HoldSeatUseCase.Input(
									seatHoldUuid = UUID.randomUUID().toString(),
									concertId = concert.concertId!!,
									seatId = seats.get(1).seatId!!,
								)
							)
						} finally {
							latch.countDown()
						}
					}
				}
				latch.await()

				seatHoldRepository.findAllConcertIdAndSeatIdAndNotExpired(concert.concertId!!, seats.map { it.seatId!! })
					.size shouldBe 1 // 하나의 좌석 예약만 성공해야 함
			}
		}
	}

})
