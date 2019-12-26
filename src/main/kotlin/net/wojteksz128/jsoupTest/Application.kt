package net.wojteksz128.jsoupTest

import net.wojteksz128.jsoupTest.dao.DAOFacadeDatabase
import net.wojteksz128.jsoupTest.scraper.ScheduleScrap
import org.jetbrains.exposed.sql.Database
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
open class Application {

    @Bean
    open fun scheduleScrap(dao: DAOFacadeDatabase) = ScheduleScrap(dao)

    @Bean
    open fun daoFacadeDatabase(
        @Value("\${datasource.url}") url: String,
        @Value("\${datasource.driver-class-name}") driver: String
    ) = DAOFacadeDatabase(Database.connect(url, driver))
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}