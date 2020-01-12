package net.wojteksz128.jsoupTest

import net.wojteksz128.jsoupTest.scraper.ScrapperScheduler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
open class Application

fun main(args: Array<String>) {
    val context = runApplication<Application>(*args)
    context.getBean(ScrapperScheduler::class.java).runScheduled()
}