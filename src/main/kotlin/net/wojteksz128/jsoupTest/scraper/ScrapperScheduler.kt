package net.wojteksz128.jsoupTest.scraper

import net.wojteksz128.jsoupTest.database.AppDatabase
import org.springframework.scheduling.annotation.Scheduled

class ScrapperScheduler(
    private val scrapper: KomputronikScrapper,
    private val appDatabase: AppDatabase
) {

    @Scheduled(cron = "0 0 6,18 * * *")
    fun runScheduled() {
        val scrappyData = scrapper.scrap()
        appDatabase.store(scrappyData)
    }
}