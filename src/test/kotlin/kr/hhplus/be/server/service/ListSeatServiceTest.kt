package kr.hhplus.be.server.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.Concert
import kr.hhplus.be.server.domain.ConcertRepository
import kr.hhplus.be.server.domain.Seat
import kr.hhplus.be.server.domain.SeatHold
import kr.hhplus.be.server.domain.SeatHoldRepository
import kr.hhplus.be.server.domain.SeatRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest
class ListSeatServiceTest @Autowired constructor(
	private val listSeatService: ListSeatService,
	private val concertRepository: ConcertRepository,
	private val seatRepository: SeatRepository,
	private val seatHoldRepository: SeatHoldRepository
) : BehaviorSpec({

	val concertId = 1L
	val price = 130_000L
	val seatsCount = 10

	beforeTest {
		concertRepository.clear()
		seatRepository.clear()
		seatHoldRepository.clear()

		concertRepository.save(
			Concert.create(
				concertId = concertId,
				title = "테스트 콘서트",
				venue = "테스트 장소",
				showDateTime = Instant.now().plusSeconds(3600 * 24 * 30) // 30일 후
			)
		)

		for (i in 1..seatsCount) {
			seatRepository.save(
				Seat.create(
					seatNumber = i,
					price = price
				)
			)
		}
	}

	given("좌석 목록을 조회할 때") {
		`when`("유효한 콘서트 ID로 조회하면") {
			val output = listSeatService.listAvailableSeats(concertId, userId = 1L)

			then("콘서트 ID와 일치하는 좌석 목록이 반환되어야 한다") {
				output.concertId shouldBe concertId
				output.availableSeats.size shouldBe seatsCount
			}
		}
		`when`("점유할 수 없는 좌석이 있다면") {
			seatHoldRepository.save(
				SeatHold.create(
					seatHoldUuid = "uuid-8",
					userId = 1L,
					concertId = concertId,
					seatId = 1L,
					status = SeatHold.Status.HELD,
				)
			)
			seatHoldRepository.save(
				SeatHold.create(
					seatHoldUuid = "uuid-8",
					userId = 8L,
					concertId = concertId,
					seatId = 8L,
					status = SeatHold.Status.HELD,
				)
			)
			seatHoldRepository.save(
				SeatHold.create(
					seatHoldUuid = "uuid-9",
					userId = 9L,
					concertId = concertId,
					seatId = 9L,
					status = SeatHold.Status.RESERVED,
				)
			)

			seatHoldRepository.save(
				SeatHold.create(
					seatHoldUuid = "uuid-10",
					userId = 10L,
					concertId = concertId,
					seatId = 10L,
					status = SeatHold.Status.EXPIRED,
				)
			)
			val output = listSeatService.listAvailableSeats(concertId, userId = 1L)

			then("각 좌석의 상태가 반영되어야 한다") {
				output.availableSeats.forEach { seatInfo ->
					seatInfo.seatId shouldBeGreaterThan 0L
					seatInfo.price shouldBe price
					if(seatInfo.seatId >= 8L) seatInfo.isAvailable shouldBe false
					else seatInfo.isAvailable shouldBe true
				}
			}
		}

		`when`("존재하지 않는 콘서트 ID로 조회하면") {
			val invalidConcertId = 999L

			then("예외가 발생해야 한다") {
				val exception = shouldThrow<IllegalArgumentException> {
					val output = listSeatService.listAvailableSeats(invalidConcertId, userId = 1L)
				}
				exception.message shouldBe "존재하지 않는 콘서트입니다."
			}
		}
	}
})
