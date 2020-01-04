package net.wojteksz128.jsoupTest.scraper

import org.springframework.scheduling.annotation.Scheduled

class ScrapperScheduler(private val scrapper: KomputronikScrapper) {

    @Scheduled(cron = "0 0 6,18 * * *")
    fun runScheduled() {
        scrapper.scrap()
    }
}