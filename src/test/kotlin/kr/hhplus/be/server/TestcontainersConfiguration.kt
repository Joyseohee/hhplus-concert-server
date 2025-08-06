// package kr.hhplus.be.server
//
// import jakarta.annotation.PreDestroy
// import org.springframework.context.annotation.Configuration
// import org.testcontainers.containers.MySQLContainer
// import org.testcontainers.utility.DockerImageName
//
// @Configuration
// class TestcontainersConfiguration {
// 	@PreDestroy
// 	fun preDestroy() {
// 		if (mySqlContainer.isRunning) mySqlContainer.stop()
// 	}
//
// 	companion object {
// 		val mySqlContainer: MySQLContainer<*> =
// 			MySQLContainer(DockerImageName.parse("mysql:8.0"))
// 				.withExposedPorts(3306)
// 				.withDatabaseName("hhplus")
// 				.withUsername("application")
// 				.withPassword("application")
// 				.apply {
// 					start()
// 				}
//
// 		init {
// 			System.setProperty(
// 				"spring.datasource.url",
// 				mySqlContainer.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC"
// 			)
// 			System.setProperty("spring.datasource.username", mySqlContainer.username)
// 			System.setProperty("spring.datasource.password", mySqlContainer.password)
//
// 			println("JDBC URL: ${mySqlContainer.jdbcUrl}")
// 			println("Username: ${mySqlContainer.username}")
// 			println("Password: ${mySqlContainer.password}")
// 		}
// 	}
// }
