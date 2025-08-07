package kr.hhplus.be.server.application

import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.KotestIntegrationSpec
import kr.hhplus.be.server.domain.model.Concert
import kr.hhplus.be.server.domain.repository.ConcertRepository
import java.time.Instant

class ListConcertUseCaseTest(
	private val listConcertUseCase: ListConcertUseCase,
	private val concertRepository: ConcertRepository
) : KotestIntegrationSpec({
	val COUNT = 5
	val DAYS_INTERVAL = 10L
	val SECONDS_PER_DAY = 24 * 3600L
	val VENUE = "서울 올림픽공원 체조경기장"
	val TITLE_PREFIX = "HH+ Concert"

	// 매 테스트 전 DB 초기화
	beforeEach {
		concertRepository.clear()
	}

	given("콘서트 목록을 조회할 때") {
		`when`("콘서트 정보를 요청하면") {
			then("콘서트 정보가 날짜순으로 정렬되어 반환된다") {
				val baseTime = Instant.now()
				val concerts = (1..COUNT).map { i ->
					Concert.create(
						showDateTime = baseTime.minusSeconds(SECONDS_PER_DAY * DAYS_INTERVAL * i),
						venue = VENUE,
						title = "$TITLE_PREFIX $i"
					)
				}
				concertRepository.saveAll(concerts)

				val output = listConcertUseCase.listConcerts()

				output.availableDates.size shouldBe COUNT
				output.availableDates[0].concertDateTime shouldBe concerts[0].showDateTime.toString()
			}
		}
	}
})
