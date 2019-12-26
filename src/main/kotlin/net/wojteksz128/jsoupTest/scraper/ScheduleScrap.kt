package net.wojteksz128.jsoupTest.scraper

import net.wojteksz128.jsoupTest.dao.DAOFacadeDatabase
import net.wojteksz128.jsoupTest.model.PcSpecDto
import net.wojteksz128.jsoupTest.model.Specification
import org.jetbrains.exposed.sql.Database
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduleScrap {

    val dao = DAOFacadeDatabase(Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver"))

    @Scheduled(cron = "0 0-59/5 * * * *")
    fun main() {
        val doc = Jsoup.connect("https://www.komputronik.pl/category/5801/komputery-pc.html").get()
        println(doc.title() + "\n")
        val newsHeadlines = doc.select("#products-list > div .pe2-head a.blank-link")
        dao.init()
        fetchPcList(newsHeadlines)
    }

    fun fetchPcList(newsHeadlines: Elements) {
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

    fun getPCSpecsAndInsertToDB(absURL: String, name: String)/*: Computer*/ {
        val doc = Jsoup.connect(absURL).get()
        var price: Int = 0
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

    fun putParametersIntoMap(doc: Document): MutableMap<String, MutableList<String>> {
        val specs: MutableMap<String, MutableList<String>> = mutableMapOf()
        val specHeadlines =
            doc.select("#p-content-specification .full-specification") //tutaj przechodzi do tabeli ze specyfikacją kompa

        for (headline in specHeadlines) {
            if (headline.toString().contains("ng-")) {
                continue
            }

            val tableHeader = headline.select(".section table tr")
            for (tabHeader in tableHeader) {
                var specDetails: MutableList<String> = ArrayList()
                val specName: String = tabHeader.select("th").text()
                val spec = tabHeader.select("td")

                if (spec.select("br").size > 1) {
                    if (spec.html().contains("href")) {
                        for (s in spec.select("br").get(0).parent().children().select("a").html().split("\n")) {
                            if (!s.equals("")) {
                                specDetails.add(s)
                            }
                        }
                        continue
                    }

                    spec.html().split("<br>")

                    for (s in spec.html().split("<br>")) {
                        if (!s.equals("")) {
                            specDetails.add(s)
                        }
                    }
                } else
                    specDetails.add(tabHeader.select("td").text());
                specs.putIfAbsent(specName, specDetails)
            }
        }
        return specs
    }

    /* metoda, której można użyć, żeby przekonwertować listę PcSpecDto na mapę - pole z data klasy Specification */
    fun convertPcSpecDtoToSpecification(spec: List<PcSpecDto>, pcId: Int): Specification {
        val parameters: MutableMap<String, MutableList<String>> = mutableMapOf()

        for (param in spec) {

            if (parameters.containsKey(param.attributeName)) {
                val tmpParamVal: MutableList<String> = parameters.get(param.attributeName)!!
                tmpParamVal.add(param.attribuiteValue)
                parameters.replace(param.attributeName, tmpParamVal)
            } else {
                var paramValue: MutableList<String> = ArrayList()
                paramValue.add(param.attribuiteValue)
                parameters.putIfAbsent(param.attributeName, paramValue)
            }
        }
        return Specification(pcId, parameters)
    }
}