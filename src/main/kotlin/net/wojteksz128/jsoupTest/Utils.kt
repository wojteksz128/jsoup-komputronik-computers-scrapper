package net.wojteksz128.jsoupTest

import com.panforge.robotstxt.RobotsTxt
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

fun String.masterLabel() = "\n" +
        "==================================================\n" +
        "$this\n" +
        "==================================================\n"

fun String.groupLabel() = "\n" +
        "--------------------------------------------------\n" +
        "$this\n" +
        "--------------------------------------------------\n"

fun String.stepLabel() = "---------- $this ----------"

class PageGetter(robotsFileUrl: String) {
    private val log: Logger = LoggerFactory.getLogger(PageGetter::class.java)
    private var robotsTxt: RobotsTxt = RobotsTxt.read(URL(robotsFileUrl).openStream())

    fun get(url: String): Document {
        if (robotsTxt.query(null, url)) return Jsoup.connect(url).get()
        else {
            log.warn("Page not allowed by robots.txt: $url")
            throw PageNotAllowedException(url)
        }
    }
}

class PageNotAllowedException(url: String) : Exception("Page not alowed: $url")
