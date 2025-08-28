package kr.hhplus.be.server.application

import kr.hhplus.be.server.domain.repository.ConcertAggregationRepository
import kr.hhplus.be.server.domain.repository.ReservationRepository
import org.springframework.stereotype.Service

@Service
class AggregatePopularConcertUseCase(
    private val reservationRepository: ReservationRepository,
    private val concertAggregationRepository: ConcertAggregationRepository,
) {
    fun execute(reservationId: Long) {
        val reservation = reservationRepository.findByIdOrElseThrow(reservationId)
        concertAggregationRepository.incrementScore("popular:concerts", reservation!!.concertId)
    }
}