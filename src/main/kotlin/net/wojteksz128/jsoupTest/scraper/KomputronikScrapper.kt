package net.wojteksz128.jsoupTest.scraper

import net.wojteksz128.jsoupTest.*
import net.wojteksz128.jsoupTest.model.Computer
import net.wojteksz128.jsoupTest.model.ComputerSpecification
import net.wojteksz128.jsoupTest.model.ComputerSpecificationAssignation
import net.wojteksz128.jsoupTest.model.ComputerSpecificationValue
import org.joda.time.DateTime
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.MessageFormat.format
import java.util.*
import java.util.stream.IntStream

interface KomputronikScrapper {
    fun scrap(): ScrappyData
}

private const val ROBOTS_FILE_PATH = "https://www.komputronik.pl/robots.txt"
private const val SCRAPPER_START_ADDRESS = "https://www.komputronik.pl/category/5801/komputery-pc.html"
private const val BASE_PAGE_WITH_PAGE_NO = "$SCRAPPER_START_ADDRESS?p={0}"

private const val PRODUCT_LIST_ITEM_HEADER_SELECTOR = "#products-list > div .pe2-head a.blank-link"
private const val PAGINATION_ITEM_SELECTOR = ".pagination.sp-top-grey:not([ng-cloak]) a"
private const val FULL_PRODUCT_SPECIFICATION_SELECTOR = "#p-content-specification .full-specification"

class KomputronikScrapperImpl : KomputronikScrapper {
    private val log: Logger = LoggerFactory.getLogger(KomputronikScrapperImpl::class.java)

    override fun scrap(): ScrappyData {
        val scrappyData = ScrappyData(DateTime.now())
        val pageGetter = PageGetter(ROBOTS_FILE_PATH)

        log.info("Scrapping started".masterLabel())
        try {
            IntStream.rangeClosed(1, getPagesNo(pageGetter))
                .forEach { pageNo -> fetchPCsFromPage(pageNo, scrappyData, pageGetter) }
            scrappyData.scrapInstance.endDate = DateTime.now()
        } catch (ignored: PageNotAllowedException) {
        }
        log.info("Scrapping ended".masterLabel())

        return scrappyData
    }

    private fun getPagesNo(pageGetter: PageGetter): Int {
        return pageGetter.get(SCRAPPER_START_ADDRESS)
            .select(PAGINATION_ITEM_SELECTOR)
            ?.stream()
            ?.map { it.text().toIntOrNull() }
            ?.filter(Objects::nonNull)
            ?.mapToInt { value -> value!! }
            ?.max()
            ?.orElse(1)
            ?: 1
    }

    private fun fetchPCsFromPage(
        pageNo: Int,
        scrappyData: ScrappyData,
        pageGetter: PageGetter
    ) {
        log.info("Page $pageNo".stepLabel())
        try {
            val pageDocument = pageGetter.get(format(BASE_PAGE_WITH_PAGE_NO, pageNo))
            fetchPCsFromDocument(pageDocument, scrappyData, pageGetter)
        } catch (ignored: PageNotAllowedException) {
        }
    }

    private fun fetchPCsFromDocument(mainDocument: Document, scrappyData: ScrappyData, pageGetter: PageGetter) {
        mainDocument.select(PRODUCT_LIST_ITEM_HEADER_SELECTOR)
            .parallelStream()
            .forEach { headline -> readHeadline(headline, scrappyData, pageGetter) }
    }

    private fun readHeadline(headline: Element, scrappyData: ScrappyData, pageGetter: PageGetter) {
        if (headline.isWebFrameworkLayout()) return

        if (headline.hasHrefToPCGroup()) {
            log.info("${headline.childNodes().first()}".groupLabel())
            try {
                val computerGroupDocument = pageGetter.get(headline.absUrl("href"))
                fetchPCsFromDocument(computerGroupDocument, scrappyData, pageGetter)
            } catch (ignored: PageNotAllowedException) {
            }
        } else {
            val computer =
                Computer(headline.childNodes().first().toString(), headline.absUrl("href"), scrappyData.scrapInstance)
            log.info("${computer.name}\n\t${computer.url}")
            scrappyData.scrapInstance.computers.add(computer)
            fetchPCInformation(computer, scrappyData, pageGetter)
        }
    }

    private fun fetchPCInformation(computer: Computer, scrappyData: ScrappyData, pageGetter: PageGetter) {
        try {
            val doc = pageGetter.get(computer.url)
            computer.price = readPrice(doc)
            computer.specs.addAll(readAllSpecs(doc, computer, scrappyData))
        } catch (ignored: PageNotAllowedException) {
        }
    }

    private fun readPrice(doc: Document): Double {
        var price = 0.0
        val priceTag = doc.select("#p-inner-prices .price")
        for (priceElement in priceTag) {
            if (priceElement.select("span").toString().contains("small")) {
                price =
                    priceElement.select("span.proper").html().replace("&nbsp;", "").substringBefore("<")
                        .replace(",", ".").trim().toDouble()
            }
        }
        return price
    }

    private fun readAllSpecs(
        doc: Document,
        computer: Computer,
        scrappyData: ScrappyData
    ): Iterable<ComputerSpecificationAssignation> {
        val specs: MutableSet<ComputerSpecificationAssignation> = mutableSetOf()
        val specTables = doc.select(FULL_PRODUCT_SPECIFICATION_SELECTOR)

        for (specTable in specTables) {
            if (specTable.toString().contains("ng-")) continue

            for (tableSpecRow in specTable.select(".section table tr")) {
                val specification = getAndAddSpec(tableSpecRow, scrappyData)
                val value = getAndAddSpecValues(tableSpecRow, specification, scrappyData)
                value.forEach { specs.add(ComputerSpecificationAssignation(computer, specification, it)) }
            }
        }
        return specs
    }

    private fun getAndAddSpec(tableSpecRow: Element, scrappyData: ScrappyData): ComputerSpecification {
        val specName = tableSpecRow.select("th").text().trim()
        return scrappyData.addOrGetProperty(ComputerSpecification(specName))
    }

    private fun getAndAddSpecValues(
        tableSpecRow: Element,
        specification: ComputerSpecification,
        scrappyData: ScrappyData
    ): MutableSet<ComputerSpecificationValue> {
        val specValuesCell = tableSpecRow.select("td")
        val specValues = mutableSetOf<ComputerSpecificationValue>()

        if (specValuesCell.select("br").size > 1) {
            val valueList = if (specValuesCell.html().contains("href"))
                specValuesCell.select("br")[0].parent().children().select("a").html().split("\n")
            else specValuesCell.html().split("<br>")

            for (value in valueList)
                if (value.trim().isNotEmpty()) specValues += scrappyData.addOrGetPropertyValue(
                    specification,
                    ComputerSpecificationValue(specification, value.trim())
                )

        } else specValues += scrappyData.addOrGetPropertyValue(
            specification,
            ComputerSpecificationValue(specification, specValuesCell.text().trim())
        )

        return specValues
    }
}

private fun Element.isWebFrameworkLayout() = childNodes().first().toString().startsWith(" {{")

private fun Element.hasHrefToPCGroup() = absUrl("href").contains("productVariantGroup")
