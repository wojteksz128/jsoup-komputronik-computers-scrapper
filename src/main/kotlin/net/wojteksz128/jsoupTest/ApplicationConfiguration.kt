package net.wojteksz128.jsoupTest

import net.wojteksz128.jsoupTest.dao.DAOFacadeDatabase
import net.wojteksz128.jsoupTest.dao.DAOFacadePc
import net.wojteksz128.jsoupTest.scraper.KomputronikScrapper
import net.wojteksz128.jsoupTest.scraper.KomputronikScrapperImpl
import net.wojteksz128.jsoupTest.scraper.ScrapperScheduler
import org.jetbrains.exposed.sql.Database
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class ApplicationConfiguration {

    @Value("\${datasource.url}")
    private lateinit var url: String

    @Value("\${datasource.driver-class-name}")
    private lateinit var driver: String


    @Bean
    open fun komputronikScrapper(dao: DAOFacadePc): KomputronikScrapper = KomputronikScrapperImpl(dao)

    @Bean
    open fun daoFacadeDatabase(): DAOFacadePc {
        val database = DAOFacadeDatabase(Database.connect(url, driver))
        database.init()
        return database
    }

    @Bean
    open fun runBenchmark(komputronikScrapper: KomputronikScrapper): CommandLineRunner {
        return CommandLineRunner { komputronikScrapper.scrap() }
    }

    @Bean
    open fun scrapperScheduler(komputronikScrapper: KomputronikScrapper): ScrapperScheduler =
        ScrapperScheduler(komputronikScrapper)
}