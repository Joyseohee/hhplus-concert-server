package kr.hhplus.be.server.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI/Swagger 설정
 */
@Configuration
@OpenAPIDefinition(
	info = Info(
		title       = "콘서트 예약 서비스 API",
		version     = "1.0.0",
		description = "대기열 관리, 좌석 점유, 결제 기능을 제공하는 콘서트 예약 서비스",
		contact     = Contact(name = "박서희", email = "sh940311@gmail.com"),
	),
	servers = [
		Server(url = "http://localhost:8080", description = "Local 개발 서버"),
		// Server(url = "https://api.hhplus.kr", description = "Production 서버")
	],
	tags = [
		Tag(name = "Queue", description = "대기열 토큰 조회/발급"),
		Tag(name = "Seats",     description = "좌석 조회/점유"),
		Tag(name = "Balance",   description = "잔액 조회/충전"),
		Tag(name = "Reservations",   description = "결제 및 예약")
	]
)
class SwaggerConfig {

	// 0. 전체 API
	@Bean
	fun allApi(): GroupedOpenApi = GroupedOpenApi.builder()
		.group("All")
		.pathsToMatch("/**")
		.build()


	// 1. Queue 관련 API
	@Bean
	fun queueApi(): GroupedOpenApi = GroupedOpenApi.builder()
		.group("Queue")
		.pathsToMatch("/queue/**")
		.build()

	// 2. Seat(스케줄/좌석) 관련 API
	@Bean
	fun seatApi(): GroupedOpenApi = GroupedOpenApi.builder()
		.group("Seat")
		.pathsToMatch(
			"/schedules/**",
			"/seats/**"
		)
		.build()

	// 3. Balance(잔액) 관련 API
	@Bean
	fun balanceApi(): GroupedOpenApi = GroupedOpenApi.builder()
		.group("Balance")
		.pathsToMatch("/balance/**")
		.build()

	// 4. Payment(결제/예약) 관련 API
	@Bean
	fun paymentApi(): GroupedOpenApi = GroupedOpenApi.builder()
		.group("Reservations")
		.pathsToMatch("/reservations/**")
		.build()
}