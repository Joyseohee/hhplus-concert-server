package kr.hhplus.be.server.application

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.repository.ConcertRepository
import org.springframework.stereotype.Service

@Service
class ListConcertUseCase(
	private val concertRepository: ConcertRepository
) {

	fun listConcerts(): Output {
		val concerts = concertRepository.findAllOrderByShowDateTime()

		return Output(availableDates = concerts.map { concert ->
			Output.ConcertInfo(
				concertId = concert.concertId!!,
				concertDateTime = concert.showDateTime.toString(),
				concertVenue = concert.venue,
				concertTitle = concert.title,
				isAvailable = concert.showDateTime.isAfter(java.time.Instant.now())
			)
		})
	}


	@Schema(name = "ListConcertRequest", description = "콘서트 목록 조회 요청")
	data class Output(
		@Schema(description = "콘서트 목록")
		val availableDates: List<ConcertInfo>
	) {
		data class ConcertInfo(
			@Schema(description = "콘서트 id", example = "1")
			val concertId: Long,
			@Schema(description = "콘서트 일시", example = "2023-10-01T19:00:00")
			val concertDateTime: String,
			@Schema(description = "콘서트 장소", example = "서울 올림픽공원 체조경기장")
			val concertVenue: String,
			@Schema(description = "콘서트 제목", example = "2023 HH+ Concert")
			val concertTitle: String,
			@Schema(description = "예약 가능 여부", example = "true")
			val isAvailable: Boolean,
		)
	}

}
