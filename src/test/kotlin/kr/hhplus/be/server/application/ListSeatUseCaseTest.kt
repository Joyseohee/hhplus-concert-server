package kr.hhplus.be.server.application

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.domain.model.Concert
import kr.hhplus.be.server.domain.model.Reservation
import kr.hhplus.be.server.domain.model.Seat
import kr.hhplus.be.server.domain.model.SeatHold
import kr.hhplus.be.server.domain.repository.ConcertRepository
import kr.hhplus.be.server.domain.repository.ReservationRepository
import kr.hhplus.be.server.domain.repository.SeatHoldRepository
import kr.hhplus.be.server.domain.repository.SeatRepository
import java.time.Instant
import java.util.*

class ListSeatUseCaseTest(
	private val listSeatUseCase: ListSeatUseCase,
	private val concertRepository: ConcertRepository,
	private val seatRepository: SeatRepository,
	private val seatHoldRepository: SeatHoldRepository,
	private val reservationRepository: ReservationRepository
) : KotestIntegrationSpec({


	beforeEach {
		reservationRepository.clear()
		seatHoldRepository.clear()
		seatRepository.clear()
		concertRepository.clear()
	}

	given("좌석 목록 조회를 요청할 때") {
		`when`("존재하지 않는 콘서트 ID를 넘기면") {
			then("예외가 발생한다") {
				shouldThrowExactly<IllegalArgumentException> {
					listSeatUseCase.listAvailableSeats(
						concertId = 999L,
						userId = 1L
					)
				}.message?.contains("콘서트를 찾을 수 없습니다") shouldBe true
			}
		}

		`when`("콘서트는 있지만 좌석이 하나도 없으면") {
			then("빈 목록을 반환한다") {
				val concert = concertRepository.save(
					Concert.create(
						title = "Test Concert",
						venue = "Test Venue",
						showDateTime = Instant.now().plusSeconds(3600)
					)
				)

				val output = listSeatUseCase.listAvailableSeats(
					concertId = concert.concertId!!,
					userId = 1L
				)
				output.availableSeats.shouldBeEmpty()
			}
		}

		`when`("여러 좌석이 있고 점유·예약 이력이 없으면") {
			then("모든 좌석이 available=true로 반환된다") {
				val concert = concertRepository.save(
					Concert.create(title = "C", venue = "V", showDateTime = Instant.now().plusSeconds(3600))
				)
				val s1 = seatRepository.save(Seat.create(seatNumber = 1, price = 10_000L))
				val s2 = seatRepository.save(Seat.create(seatNumber = 2, price = 20_000L))

				val output = listSeatUseCase.listAvailableSeats(
					concertId = concert.concertId!!,
					userId = 1L
				)

				output.availableSeats.map { it.seatId } shouldBe listOf(s1.seatId, s2.seatId)
				output.availableSeats.all { it.isAvailable } shouldBe true
				output.availableSeats.map { it.seatNumber } shouldBe listOf(1, 2)
			}
		}

		`when`("다른 사용자가 점유한 좌석이 있으면") {
			then("해당 좌석은 available=false로 표시된다") {
				val concert = concertRepository.save(
					Concert.create(title = "C", venue = "V", showDateTime = Instant.now().plusSeconds(3600))
				)
				val seat = seatRepository.save(Seat.create(seatNumber = 1, price = 50_000))
				seatHoldRepository.save(
					SeatHold.create(
						seatHoldUuid = UUID.randomUUID().toString(),
						userId = 999L,
						concertId = concert.concertId!!,
						seatId = seat.seatId!!
					)
				)

				val output = listSeatUseCase.listAvailableSeats(
					concertId = concert.concertId,
					userId = 1L
				)

				val info = output.availableSeats.single { it.seatId == seat.seatId }
				info.isAvailable shouldBe false
			}
		}

		`when`("만료된 점유는 ignored 되고 available=true가 유지되면") {
			then("만료된 좌석도 available=true로 반환된다") {
				val concert = concertRepository.save(
					Concert.create(title = "C", venue = "V", showDateTime = Instant.now().plusSeconds(3600))
				)
				val seat = seatRepository.save(Seat.create(seatNumber = 1, price = 50_000))
				seatHoldRepository.save(
					SeatHold.create(
						seatHoldUuid = UUID.randomUUID().toString(),
						userId = 999L,
						concertId = concert.concertId!!,
						seatId = seat.seatId!!,
						expiresAt = Instant.now().minusSeconds(60)
					)
				)

				val output = listSeatUseCase.listAvailableSeats(
					concertId = concert.concertId,
					userId = 1L
				)

				val info = output.availableSeats.single { it.seatId == seat.seatId }
				info.isAvailable shouldBe true
			}
		}

		`when`("이미 예약된 좌석이 있으면") {
			then("해당 좌석은 available=false로 표시된다") {
				val concert = concertRepository.save(
					Concert.create(title = "C", venue = "V", showDateTime = Instant.now().plusSeconds(3600))
				)
				val seat = seatRepository.save(Seat.create(seatNumber = 1, price = 50_000))
				reservationRepository.save(
					Reservation.create(
						reservationUuid = UUID.randomUUID().toString(),
						userId = 888L,
						concertId = concert.concertId!!,
						seatId = seat.seatId!!,
						reservedAt = Instant.now(),
						price = 5_000L
					)
				)

				val output = listSeatUseCase.listAvailableSeats(
					concertId = concert.concertId,
					userId = 1L
				)

				val info = output.availableSeats.single { it.seatId == seat.seatId }
				info.isAvailable shouldBe false
			}
		}
	}
})
