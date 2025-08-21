package kr.hhplus.be.server.application

import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.domain.model.Concert
import kr.hhplus.be.server.domain.model.QueueToken
import kr.hhplus.be.server.domain.model.Seat
import kr.hhplus.be.server.domain.model.SeatHold
import kr.hhplus.be.server.domain.model.UserBalance
import kr.hhplus.be.server.domain.repository.ConcertAggregationRepository
import kr.hhplus.be.server.domain.repository.ConcertRepository
import kr.hhplus.be.server.domain.repository.QueueTokenRepository
import kr.hhplus.be.server.domain.repository.ReservationRepository
import kr.hhplus.be.server.domain.repository.SeatHoldRepository
import kr.hhplus.be.server.domain.repository.SeatRepository
import kr.hhplus.be.server.domain.repository.UserBalanceRepository
import java.time.Instant
import java.util.UUID

class ListPopularConcertUseCaseTest(
	private val listPopularConcertUseCase: ListPopularConcertUseCase,
	private val confirmReservationUseCase: ConfirmReservationUseCase,
	private val concertRepository: ConcertRepository,
	private val concertAggregationRepository: ConcertAggregationRepository,
	private val seatHoldRepository: SeatHoldRepository,
	private val queueTokenRepository: QueueTokenRepository,
	private val seatRepository: SeatRepository,
	private val reservationRepository: ReservationRepository,
	private val userBalanceRepository: UserBalanceRepository,
	) : KotestIntegrationSpec({

	val SEAT_NUMBER = 10
	val PRICE = 50_000L

	// 매 테스트 전 DB 초기화
	beforeEach {
		concertRepository.clear()
		reservationRepository.clear()
		seatHoldRepository.clear()
		seatRepository.clear()
		userBalanceRepository.clear()
		queueTokenRepository.clear()
		concertAggregationRepository.clear("popular:concerts")
	}

	given("인기 콘서트 목록을 조회할 때") {
		`when`("인기 콘서트 정보를 요청하면") {
			then("인기 콘서트 정보가 반환된다") {
				val concert1 = concertRepository.save(
					Concert.create(
						showDateTime = Instant.now().plusSeconds(3600),
						venue = "서울 올림픽공원 체조경기장",
						title = "HH+ Concert 1"
					)
				)
				val concert2 = concertRepository.save(
					Concert.create(
						showDateTime = Instant.now().plusSeconds(7200),
						venue = "서울 올림픽공원 체조경기장",
						title = "HH+ Concert 2"
					)
				)

				// Redis에 인기 콘서트 ID 저장
				concertAggregationRepository.incrementScore("popular:concerts", concert1.concertId!!, 101.0)
				concertAggregationRepository.incrementScore("popular:concerts", concert2.concertId!!, 100.0)

				val output = listPopularConcertUseCase.listPopularConcert()

				output.popularConcert.size shouldBe 2
				output.popularConcert[0].concertId shouldBe concert1.concertId
				output.popularConcert[1].concertId shouldBe concert2.concertId
			}
		}
	}

	given("예약에서 인기 콘서트 정보를 갱신하면") {
		`when`("인기 콘서트 목록을 조회할 때") {
			then("인기 콘서트 정보가 반환된다") {
				val concert1 = concertRepository.save(
					Concert.create(
						showDateTime = Instant.now().plusSeconds(3600),
						venue = "서울 올림픽공원 체조경기장",
						title = "HH+ Concert 1"
					)
				)
				val concert2 = concertRepository.save(
					Concert.create(
						showDateTime = Instant.now().plusSeconds(7200),
						venue = "서울 올림픽공원 체조경기장",
						title = "HH+ Concert 2"
					)
				)

				// Redis에 인기 콘서트 ID 저장
				concertAggregationRepository.incrementScore("popular:concerts", concert1.concertId!!, 100.0)
				concertAggregationRepository.incrementScore("popular:concerts", concert2.concertId!!, 100.0)

				// 예약 확정 후 인기 콘서트 점수 증가
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
						concertId = concert1.concertId,
						seatId = seat.seatId!!
					)
				)

				queueTokenRepository.save(
					QueueToken.create(userId = user.userId, status = QueueToken.Status.ACTIVE)
				)

				confirmReservationUseCase.confirmReservation(
					userId = user.userId,
					input = ConfirmReservationUseCase.Input(
						reservationUuid = "reservation-uuid",
						seatHoldUuid = hold.seatHoldUuid
					)
				)

				val output = listPopularConcertUseCase.listPopularConcert()

				output.popularConcert.size shouldBe 2
				output.popularConcert[0].concertId shouldBe concert1.concertId
				output.popularConcert[1].concertId shouldBe concert2.concertId
			}
		}
	}


})
