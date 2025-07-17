package kr.hhplus.be.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ConcertApplication

fun main(args: Array<String>) {
	runApplication<ConcertApplication>(*args)
}