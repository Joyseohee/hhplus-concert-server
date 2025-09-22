package kr.hhplus.be.server.application

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.model.Concert
import kr.hhplus.be.server.domain.repository.ConcertRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class CreateConcertUseCase(
	private val concertRepository: ConcertRepository
) {

	fun execute(): Output {
		val concerts = concertRepository.save(
			Concert.create(
				showDateTime = Instant.parse("2023-10-01T19:00:00Z"),
				venue = "서울 올림픽공원 체조경기장",
				title = "2023 HH+ Concert"
			)
		)

		return Output(
			concertId = concerts.concertId!!,
			concertDateTime = concerts.showDateTime.toString(),
			concertVenue = concerts.venue,
			concertTitle = concerts.title
		)
	}

	@Schema(name = "ListConcertRequest", description = "콘서트 목록 조회 요청")
	data class Output(
		@Schema(description = "콘서트 id", example = "1")
		val concertId: Long,
		@Schema(description = "콘서트 일시", example = "2023-10-01T19:00:00")
		val concertDateTime: String,
		@Schema(description = "콘서트 장소", example = "서울 올림픽공원 체조경기장")
		val concertVenue: String,
		@Schema(description = "콘서트 제목", example = "2023 HH+ Concert")
		val concertTitle: String,
	)
}
