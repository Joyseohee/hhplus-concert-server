package kr.hhplus.be.server.concurrency

import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.application.ConfirmReservationUseCase
import kr.hhplus.be.server.application.RequestQueueTokenUseCase
import kr.hhplus.be.server.application.schedule.ExpireStatusScheduler
import kr.hhplus.be.server.application.validation.ValidateQueueTokenService
import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.model.QueueToken.Companion.MAX_ACTIVE_COUNT
import kr.hhplus.be.server.domain.model.Seat
import kr.hhplus.be.server.domain.model.SeatHold
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.*
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class TokenConcurrencyTest @Autowired constructor(
	private val requestQueueTokenUseCase: RequestQueueTokenUseCase,
	private val confirmReservationUseCase: ConfirmReservationUseCase,
	private val validateQueueTokenService: ValidateQueueTokenService,
	private val expireStatusScheduler: ExpireStatusScheduler,
	private val userBalanceRepository: UserBalanceRepository,
	private val queueTokenRepository: QueueTokenRepository,
	private val seatHoldRepository: SeatHoldRepository,
	private val reservationRepository: ReservationRepository,
	private val seatRepository: SeatRepository
) : KotestIntegrationSpec({

	beforeEach {
		userBalanceRepository.clear()
		queueTokenRepository.clear()
		seatHoldRepository.clear()
		reservationRepository.clear()
		seatRepository.clear()
	}

	val initialBalance = 100_000L

	given("동시성 테스트 - 토큰 발급") {
		`when`("여러 스레드가 동시에 토큰을 발급할 때") {
			then("모든 요청이 성공되어야 하며 적절한 수만큼만 active 토큰이 생성되어야 한다") {
				val overCount = 10
				val threadCount = MAX_ACTIVE_COUNT + overCount
				val latch = CountDownLatch(threadCount)
				val executor = Executors.newFixedThreadPool(threadCount)

				repeat(threadCount) {
					executor.submit {
						try {
							// 사용자 초기화
							val userBalance = userBalanceRepository.save(UserBalance.create(balance = initialBalance))

							// 토큰 요청
							requestQueueTokenUseCase.createToken(userId = userBalance.userId!!)
						} catch (e: Exception) {
							e.printStackTrace()
						} finally {
							latch.countDown()
						}
					}
				}
				latch.await()
				Thread.sleep((50 / 5 + 1) * 5000) // 반영 대기
				// 최종 상태 확인
				val queueTokens = queueTokenRepository.findAll()

				// 모든 토큰이 발행되었는지 확인
				queueTokens.size shouldBe threadCount

				// 토큰의 상태가 모두 ACTIVE인지 확인
				queueTokens.filter { it.status == QueueToken.Status.ACTIVE }.size shouldBeGreaterThanOrEqualTo  MAX_ACTIVE_COUNT

				// 토큰의 생성 시간은 현재 시간보다 이전이어야 함
				queueTokens.all { it.createdAt.isBefore(Instant.now()) } shouldBe true
			}
		}
	}

	given("동시성 테스트 - 토큰 만료와 토큰 사용") {
		`when`("여러 스레드가 동시에 토큰을 만료시키고 사용하려고 할 때") {
			then("모든 요청이 성공되어야 하며, 만료된 토큰은 사용할 수 없어야 한다") {
				val threadCount = 10
				val latch = CountDownLatch(threadCount)
				val executor = Executors.newFixedThreadPool(threadCount)
				val userTokens = mutableListOf<Pair<Long, String>>()

				// 여러 유저에게 토큰 발급
				repeat(threadCount) {
					val userBalance = userBalanceRepository.save(UserBalance.create(balance = initialBalance))
					val token = requestQueueTokenUseCase.createToken(userId = userBalance.userId!!)
					userTokens.add(userBalance.userId!! to token.token)
				}

				// 절반의 토큰을 만료(EXPIRED) 상태로 변경 (expiresAt을 과거로 설정)
				val now = Instant.now()
				val expiredTokens = userTokens.take(threadCount / 2)
				expiredTokens.forEach { (userId, token) ->
					val queueToken = queueTokenRepository.findByToken(token)
					if (queueToken != null) {
						val expired = queueToken.copy(status = QueueToken.Status.ACTIVE, expiresAt = now.minusSeconds(60))
						queueTokenRepository.save(expired)
					}
				}

				// 여러 스레드가 동시에 토큰 사용 시도 (validate)
				val results = Collections.synchronizedList(mutableListOf<Boolean>())
				userTokens.forEach { (userId, token) ->
					executor.submit {
						try {
							// 토큰 검증 시도
							validateQueueTokenService.validateToken(token)
							results.add(true)
						} catch (e: Exception) {
							results.add(false)
						} finally {
							latch.countDown()
						}
					}
				}
				latch.await()

				// 만료된 토큰은 실패, 유효한 토큰만 성공
				val successCount = results.count { it }
				val failCount = results.count { !it }
				successCount shouldBe (threadCount / 2)
				failCount shouldBe (threadCount / 2)
			}
		}
	}

})
