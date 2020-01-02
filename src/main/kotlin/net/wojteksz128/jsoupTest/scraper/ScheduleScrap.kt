package net.wojteksz128.jsoupTest.scraper

import net.wojteksz128.jsoupTest.dao.DAOFacadePc
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.springframework.scheduling.annotation.Scheduled

class ScheduleScrap(private val dao: DAOFacadePc) {

    @Scheduled(cron = "0 0-59/5 * * * *")
    fun main() {
        val doc = Jsoup.connect("https://www.komputronik.pl/category/5801/komputery-pc.html").get()
        println(doc.title() + "\n")
        val newsHeadlines = doc.select("#products-list > div .pe2-head a.blank-link")
        dao.init()
        fetchPcList(newsHeadlines)
    }

    private fun fetchPcList(newsHeadlines: Elements) {
        for (headline in newsHeadlines) {
            if (!headline.childNodes().first().toString().startsWith(" {{")) {
                if (headline.absUrl("href").contains("productVariantGroup")) {
                    val doc2 = Jsoup.connect(headline.absUrl("href")).get()
                    val newsHeadlines2 = doc2.select("#products-list > div .pe2-head a.blank-link")

                    fetchPcList(newsHeadlines2)
                } else {
                    println("${headline.childNodes().first()}\n\t${headline.absUrl("href")}")
                    getPCSpecsAndInsertToDB(
                        headline.absUrl("href"),
                        headline.childNodes().first().toString()
                    )
                }
            }
        }

    }

    private fun getPCSpecsAndInsertToDB(absURL: String, name: String)/*: Computer*/ {
        val doc = Jsoup.connect(absURL).get()
        var price = 0
        val priceTag = doc.select("#p-inner-prices .price")
        for (priceElement in priceTag) {
            if (priceElement.select("span").toString().contains("small")) {
                price =
                    priceElement.select("span.proper").html().replace("&nbsp;", "").substringBefore("<").trim().toInt()
            }
        }
        val specs: MutableMap<String, MutableList<String>> =
            putParametersIntoMap(doc)

        dao.createComputer(name, absURL, price, specs)
    }

    private fun putParametersIntoMap(doc: Document): MutableMap<String, MutableList<String>> {
        val specs: MutableMap<String, MutableList<String>> = mutableMapOf()
        val specHeadlines =
            doc.select("#p-content-specification .full-specification") //tutaj przechodzi do tabeli ze specyfikacją kompa

        for (headline in specHeadlines) {
            if (headline.toString().contains("ng-")) {
                continue
            }

            val tableHeader = headline.select(".section table tr")
            for (tabHeader in tableHeader) {
                val specDetails: MutableList<String> = ArrayList()
                val specName: String = tabHeader.select("th").text()
                val spec = tabHeader.select("td")

                if (spec.select("br").size > 1) {
                    if (spec.html().contains("href")) {
                        for (s in spec.select("br")[0].parent().children().select("a").html().split("\n")) {
                            if (s != "") {
                                specDetails.add(s)
                            }
                        }
                        continue
                    }

                    spec.html().split("<br>")

                    for (s in spec.html().split("<br>")) {
                        if (s != "") {
                            specDetails.add(s)
                        }
                    }
                } else
                    specDetails.add(tabHeader.select("td").text())
                specs.putIfAbsent(specName, specDetails)
            }
        }
        return specs
    }
}