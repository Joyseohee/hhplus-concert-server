package kr.hhplus.be.server.application.client.response

data class ReservationDataResponse(
	val resultType: ResultType
) {
	enum class ResultType {
		SUCCESS,
		FAILURE,
	}
}