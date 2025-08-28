package kr.hhplus.be.server.application

import kr.hhplus.be.server.application.client.ReservationDataClient
import kr.hhplus.be.server.application.client.request.ReservationDataRequest
import kr.hhplus.be.server.application.client.response.ReservationDataResponse
import kr.hhplus.be.server.domain.repository.ReservationRepository
import org.springframework.stereotype.Service

@Service
class SendReservationDataUseCase(
	private val reservationDataClient: ReservationDataClient,
	private val reservationRepository: ReservationRepository
) {
	fun execute(reservationId: Long, userId: Long) {
		val reservation = reservationRepository.findByIdOrElseThrow(reservationId)

		val response = reservationDataClient.execute(
			ReservationDataRequest(
				reservationId = reservationId,
				concertId = reservation!!.concertId,
				userId = userId,
			)
		)

		if (response == null || response.resultType == ReservationDataResponse.ResultType.FAILURE) {
			throw IllegalStateException("예약 데이터 전송에 실패했습니다. reservationId=$reservationId, userId=$userId")
		}
	}

}