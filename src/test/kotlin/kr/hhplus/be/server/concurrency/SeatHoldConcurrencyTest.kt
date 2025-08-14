package kr.hhplus.be.server.concurrency

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.application.ConfirmReservationUseCase
import kr.hhplus.be.server.application.HoldSeatUseCase
import kr.hhplus.be.server.application.schedule.ExpireStatusScheduler
import kr.hhplus.be.server.domain.model.*
import kr.hhplus.be.server.domain.repository.*
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class SeatHoldConcurrencyTest @Autowired constructor(
	private val holdSeatUseCase: HoldSeatUseCase,
	private val confirmReservationUseCase: ConfirmReservationUseCase,
	private val expireStatusScheduler: ExpireStatusScheduler,
	private val seatHoldRepository: SeatHoldRepository,
	private val concertRepository: ConcertRepository,
	private val seatRepository: SeatRepository,
	private val userBalanceRepository: UserBalanceRepository,
	private val queueTokenRepository: QueueTokenRepository,
) : KotestIntegrationSpec({

	beforeEach {
		seatHoldRepository.clear()
		concertRepository.clear()
		seatRepository.clear()
		userBalanceRepository.clear()
		queueTokenRepository.clear()
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

	given("동시성 테스트 - 좌석 점유 만료와 예약 경쟁") {
		`when`("여러 스레드가 동시에 예약을 시도하고, 그 사이 스케줄러가 좌석 점유를 만료시키는 경우") {
			then("좌석 점유가 만료된 후 예약을 시도한 스레드는 예외가 발생한다") {
				val concert = concertRepository.save(
					Concert.create(
						title = "동시성콘서트",
						venue = "테스트홀",
						showDateTime = Instant.now().plusSeconds(3600 * 24)
					)
				)
				val seat = seatRepository.save(
					Seat.create(seatNumber = 1, price = 10000L)
				)
				val user = userBalanceRepository.save(
					UserBalance.create(balance = 10000L)
				)
				val hold = seatHoldRepository.save(
					SeatHold.create(
						seatHoldUuid = UUID.randomUUID().toString(),
						userId = user.userId!!,
						concertId = concert.concertId!!,
						seatId = seat.seatId!!
					)
				)
				queueTokenRepository.save(
					QueueToken.create(userId = user.userId, status = QueueToken.Status.ACTIVE)
				)

				val threadCount = 5
				val latch = CountDownLatch(threadCount)
				val results = Collections.synchronizedList(mutableListOf<Throwable?>())
				val executor = Executors.newFixedThreadPool(threadCount)

				// 스케줄러로 seatHold 만료
				executor.submit {
					try {
						Thread.sleep(100)
						expireStatusScheduler.expireStatuses()
						results.add(null)
					} catch (e: Throwable) {
						results.add(e)
					} finally {
						latch.countDown()
					}
				}

				// 나머지 스레드는 예약 시도
				repeat(threadCount - 1) {
					executor.submit {
						try {
							confirmReservationUseCase.confirmReservation(
								user.userId,
								ConfirmReservationUseCase.Input(
									reservationUuid = UUID.randomUUID().toString(),
									seatHoldUuid = hold.seatHoldUuid
								)
							)
							results.add(null)
						} catch (e: Throwable) {
							results.add(e)
						} finally {
							latch.countDown()
						}
					}
				}
				latch.await()

				// 스케줄러 스레드는 예외 없이 끝나야 하고, 예약 스레드는 최소 1개 이상 예외가 발생해야 함
				val reservationExceptions = results.filterIsInstance<IllegalArgumentException>()
				reservationExceptions.size shouldNotBe 0
			}
		}
	}
})
