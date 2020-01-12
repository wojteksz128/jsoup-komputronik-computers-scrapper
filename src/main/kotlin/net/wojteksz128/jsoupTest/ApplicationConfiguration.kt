package net.wojteksz128.jsoupTest

import net.wojteksz128.jsoupTest.database.*
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
    @Bean
    open fun komputronikScrapper(): KomputronikScrapper = KomputronikScrapperImpl()

    @Bean
    open fun scrapperScheduler(komputronikScrapper: KomputronikScrapper, appDatabase: AppDatabase) =
        ScrapperScheduler(komputronikScrapper, appDatabase)

    @Bean
    open fun databaseConnection(
        @Value("\${datasource.url}") url: String,
        @Value("\${datasource.driver-class-name}") driver: String
    ) = Database.connect(url, driver)

    @Bean
    open fun appDatabase(
        databaseConnection: Database,
        computersFacade: ComputersFacade,
        scrapInstancesFacade: ScrapInstancesFacade,
        computerSpecificationsFacade: ComputerSpecificationsFacade,
        computerSpecificationValuesFacade: ComputerSpecificationValuesFacade,
        computerSpecificationAssignationFacade: ComputerSpecificationAssignationFacade
    ) = AppDatabase(
        databaseConnection,
        computersFacade,
        scrapInstancesFacade,
        computerSpecificationsFacade,
        computerSpecificationValuesFacade,
        computerSpecificationAssignationFacade
    )

    @Bean
    open fun computersFacade(): ComputersFacade = ComputersFacadeImpl()

    @Bean
    open fun scrapInstancesFacade(): ScrapInstancesFacade = ScrapInstancesFacadeImpl()

    @Bean
    open fun computersSpecificationsFacade(): ComputerSpecificationsFacade = ComputerSpecificationsFacadeImpl()

    @Bean
    open fun computerSpecificationValuesFacade(): ComputerSpecificationValuesFacade =
        ComputerSpecificationValuesFacadeImpl()

    @Bean
    open fun computerSpecificationAssignationFacade(): ComputerSpecificationAssignationFacade =
        ComputerSpecificationAssignationFacadeImpl()
}