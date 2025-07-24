package kr.hhplus.be.server.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.Concert
import kr.hhplus.be.server.domain.ConcertRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest
class ListConcertServiceTest @Autowired constructor(
    private val listConcertService: ListConcertService,
    private val concertRepository: ConcertRepository,
) : BehaviorSpec({

    beforeTest {
        concertRepository.clear()
        for (i in 1..5) {
            concertRepository.save(
                Concert.create(
                    concertId = i.toLong(),
                    showDateTime = Instant.now().plusSeconds(3600 * 24 * (i * 10L)), // 10일 간격으로 미래 날짜 설정
                    venue = "서울 올림픽공원 체조경기장",
                    title = "HH+ Concert $i"
                )
            )
        }
    }

    given("콘서트 목록을 조회할 때") {
        `when`("콘서트 정보를 요청하면") {
            val output = listConcertService.listConcerts()

            then("콘서트 정보가 날짜순으로 정렬되어 반환된다") {
                output.availableDates.size shouldBe 5
                output.availableDates[0].concertId shouldBe 1L
                output.availableDates[1].concertId shouldBe 2L
                output.availableDates[2].concertId shouldBe 3L
                output.availableDates[3].concertId shouldBe 4L
                output.availableDates[4].concertId shouldBe 5L
            }
        }
    }
})
