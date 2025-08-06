package kr.hhplus.be.server

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ConcertApplicationTests : SpringIntegrationTestSupport() {

	@Test
	fun contextLoads() {}

}
