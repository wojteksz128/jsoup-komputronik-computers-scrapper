package net.wojteksz128.jsoupTest

import net.wojteksz128.jsoupTest.dao.DAOFacadeDatabase
import net.wojteksz128.jsoupTest.dao.DAOFacadePc
import net.wojteksz128.jsoupTest.scraper.ScheduleScrap
import org.jetbrains.exposed.sql.Database
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class ApplicationConfiguration {

    @Value("\${datasource.url}")
    private lateinit var url: String

    @Value("\${datasource.driver-class-name}")
    private lateinit var driver: String


    @Bean
    open fun scheduleScrap(dao: DAOFacadePc) = ScheduleScrap(dao)

    @Bean
    open fun daoFacadeDatabase(): DAOFacadePc = DAOFacadeDatabase(Database.connect(url, driver))
}