package kr.hhplus.be.server.application.client

import kr.hhplus.be.server.application.client.request.ReservationDataRequest
import kr.hhplus.be.server.application.client.response.ReservationDataResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ReservationDataClient(
	private val builder: RestClient.Builder,
	@Value("\${external.reservation-service.url}") private val baseUrl: String,
) {
	fun execute(reservation: ReservationDataRequest): ReservationDataResponse? {
		return builder.baseUrl(baseUrl).build()
			.post()
			.uri("/reservations/data")
			.body(reservation)
			.retrieve()
			.body(ReservationDataResponse::class.java)
	}
}