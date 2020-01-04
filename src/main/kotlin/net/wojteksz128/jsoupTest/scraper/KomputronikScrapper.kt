package net.wojteksz128.jsoupTest.scraper

import net.wojteksz128.jsoupTest.dao.DAOFacadePc
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.MessageFormat.format
import java.time.LocalDateTime
import java.util.*
import java.util.stream.IntStream
import kotlin.collections.ArrayList

interface KomputronikScrapper {
    fun scrap()
}

private const val SCRAPPER_START_ADDRESS = "https://www.komputronik.pl/category/5801/komputery-pc.html"
private const val BASE_PAGE_WITH_PAGE_NO = "$SCRAPPER_START_ADDRESS?p={0}"

private const val PRODUCT_LIST_ITEM_HEADER_SELECTOR = "#products-list > div .pe2-head a.blank-link"

private const val PAGINATION_ITEM_SELECTOR = ".pagination.sp-top-grey:not([ng-cloak]) a"

class KomputronikScrapperImpl(private val dao: DAOFacadePc) : KomputronikScrapper {

    override fun scrap() {
        val scrappyData = ScrappyData(LocalDateTime.now())

        println(String.masterLabel("Scrapping started"))

        val pagesNo = getPagesNo()
        IntStream.rangeClosed(1, pagesNo).forEach { pageNo -> fetchPCsFromPage(pageNo, scrappyData) }

        println(String.masterLabel("Scrapping ended"))
    }

    private fun fetchPCsFromPage(pageNo: Int, scrappyData: ScrappyData) {
        println(String.masterLabel("Page $pageNo"))
        val pageDocument = Jsoup.connect(format(BASE_PAGE_WITH_PAGE_NO, pageNo)).get()
        fetchPCsFromDocument(pageDocument, scrappyData)
    }

    private fun getPagesNo(): Int {
        return Jsoup.connect(SCRAPPER_START_ADDRESS).get()
            ?.select(PAGINATION_ITEM_SELECTOR)
            ?.stream()
            ?.map { it.text().toIntOrNull() }
            ?.filter(Objects::nonNull)
            ?.mapToInt { value -> value!! }
            ?.max()
            ?.orElse(1)
            ?: 1
    }

    private fun fetchPCsFromDocument(
        mainDocument: Document,
        scrappyData: ScrappyData
    ) {
        mainDocument.select(PRODUCT_LIST_ITEM_HEADER_SELECTOR).parallelStream().forEach { headline ->
            if (headline.isWebFrameworkLayout()) return@forEach

            if (headline.hasHrefToPCGroup()) {
                println(String.groupLabel("${headline.childNodes().first()}"))
                val computerGroupDocument = Jsoup.connect(headline.absUrl("href")).get()
                fetchPCsFromDocument(computerGroupDocument, scrappyData)
            } else {
                println("${headline.childNodes().first()}\n\t${headline.absUrl("href")}")
                getPCSpecsAndInsertToDB(headline.absUrl("href"), headline.childNodes().first().toString())
            }
        }
    }

    private fun getPCSpecsAndInsertToDB(absURL: String, name: String)/*: Computer*/ {
        val doc = Jsoup.connect(absURL).get()
        var price = 0.0
        val priceTag = doc.select("#p-inner-prices .price")
        for (priceElement in priceTag) {
            if (priceElement.select("span").toString().contains("small")) {
                price =
                    priceElement.select("span.proper").html().replace("&nbsp;", "").substringBefore("<").replace(",", ".").trim().toDouble()
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

private fun String.Companion.masterLabel(text: String) = "\n" +
        "==================================================\n" +
        "${text}\n" +
        "==================================================\n"

private fun String.Companion.groupLabel(text: String) = "\n" +
        "--------------------------------------------------\n" +
        "${text}\n" +
        "--------------------------------------------------\n"

private fun Element.isWebFrameworkLayout() = childNodes().first().toString().startsWith(" {{")

private fun Element.hasHrefToPCGroup() = absUrl("href").contains("productVariantGroup")
