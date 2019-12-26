package net.wojteksz128.jsoupTest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
open class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}