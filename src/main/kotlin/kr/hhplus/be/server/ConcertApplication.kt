package kr.hhplus.be.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy

// @EnableScheduling
@EnableAspectJAutoProxy
@SpringBootApplication
class ConcertApplication

fun main(args: Array<String>) {
	runApplication<ConcertApplication>(*args)
}