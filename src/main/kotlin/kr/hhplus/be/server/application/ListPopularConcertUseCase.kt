package kr.hhplus.be.server.application

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.repository.ConcertAggregationRepository
import kr.hhplus.be.server.domain.repository.ConcertRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ListPopularConcertUseCase(
	private val concertAggregationRepository: ConcertAggregationRepository,
	private val concertRepository: ConcertRepository,
) {

	fun listPopularConcert(): Output {
		val topConcertIds = concertAggregationRepository.getTopConcertIds("popular:concerts", 10)

		val concerts = concertRepository.findByIds(topConcertIds)
		    .ifEmpty { throw IllegalArgumentException("인기 콘서트를 찾을 수 없습니다.") }
		    .sortedBy { concert -> topConcertIds.indexOf(concert.concertId) }

		return Output(concerts.mapIndexed { index, concert ->
			Output.ConcertInfo(
				rank = index + 1,
				concertId = concert.concertId!!,
				concertDateTime = concert.showDateTime.toString(),
				concertVenue = concert.venue,
				concertTitle = concert.title,
				isAvailable = concert.showDateTime.isAfter(Instant.now())
			)}
		)
	}

	@Schema(name = "ListConcertRequest", description = "콘서트 목록 조회 요청")
	data class Output(
		@Schema(description = "인기 콘서트 목록")
		val popularConcert: List<ConcertInfo>
	) {
		data class ConcertInfo(
			@Schema(description = "인기 순위", example = "1")
			val rank: Int,
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
