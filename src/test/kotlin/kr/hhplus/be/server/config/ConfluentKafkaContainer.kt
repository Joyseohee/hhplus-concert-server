package kr.hhplus.be.server.config

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.time.Duration

class ConfluentKafkaContainer(
	imageName: String = "confluentinc/cp-kafka:7.5.1"
) : GenericContainer<ConfluentKafkaContainer>(DockerImageName.parse(imageName)) {

	init {
		withEnv("KAFKA_PROCESS_ROLES", "broker,controller")
		withEnv("KAFKA_NODE_ID", "1")
		withEnv("KAFKA_LISTENERS", "PLAINTEXT://:9092,CONTROLLER://:9093")
		withEnv("KAFKA_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
		withEnv("KAFKA_CONTROLLER_QUORUM_VOTERS", "1@localhost:9093")
		withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092")
		withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
		withEnv("KAFKA_CLUSTER_ID", "test-cluster-id")
		withExposedPorts(9092)

		waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)))
	}

	fun getBootstrapServers(): String {
		val mappedPort = getMappedPort(9092)
		return "PLAINTEXT://${host}:${mappedPort}"
	}
}
