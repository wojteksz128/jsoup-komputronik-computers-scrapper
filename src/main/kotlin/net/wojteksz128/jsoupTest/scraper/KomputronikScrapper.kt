package net.wojteksz128.jsoupTest.scraper

import net.wojteksz128.jsoupTest.model.Computer
import net.wojteksz128.jsoupTest.model.ComputerSpecification
import net.wojteksz128.jsoupTest.model.ComputerSpecificationAssignation
import net.wojteksz128.jsoupTest.model.ComputerSpecificationValue
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.MessageFormat.format
import java.time.LocalDateTime
import java.util.*
import java.util.stream.IntStream

interface KomputronikScrapper {
    fun scrap(): ScrappyData
}

private const val SCRAPPER_START_ADDRESS = "https://www.komputronik.pl/category/5801/komputery-pc.html"
private const val BASE_PAGE_WITH_PAGE_NO = "$SCRAPPER_START_ADDRESS?p={0}"

private const val PRODUCT_LIST_ITEM_HEADER_SELECTOR = "#products-list > div .pe2-head a.blank-link"
private const val PAGINATION_ITEM_SELECTOR = ".pagination.sp-top-grey:not([ng-cloak]) a"
private const val FULL_PRODUCT_SPECIFICATION_SELECTOR = "#p-content-specification .full-specification"

class KomputronikScrapperImpl : KomputronikScrapper {

    override fun scrap(): ScrappyData {
        val scrappyData = ScrappyData(LocalDateTime.now())

        println(String.masterLabel("Scrapping started"))
        IntStream.rangeClosed(1, getPagesNo()).forEach { pageNo -> fetchPCsFromPage(pageNo, scrappyData) }
        scrappyData.endDate = LocalDateTime.now()
        println(String.masterLabel("Scrapping ended"))

        return scrappyData
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

    private fun fetchPCsFromPage(pageNo: Int, scrappyData: ScrappyData) {
        println(String.masterLabel("Page $pageNo"))
        val pageDocument = Jsoup.connect(format(BASE_PAGE_WITH_PAGE_NO, pageNo)).get()
        fetchPCsFromDocument(pageDocument, scrappyData)
    }

    private fun fetchPCsFromDocument(mainDocument: Document, scrappyData: ScrappyData) {
        mainDocument.select(PRODUCT_LIST_ITEM_HEADER_SELECTOR)
            .parallelStream()
            .forEach { headline -> readHeadline(headline, scrappyData) }
    }

    private fun readHeadline(headline: Element, scrappyData: ScrappyData) {
        if (headline.isWebFrameworkLayout()) return

        if (headline.hasHrefToPCGroup()) {
            println(String.groupLabel("${headline.childNodes().first()}"))
            val computerGroupDocument = Jsoup.connect(headline.absUrl("href")).get()
            fetchPCsFromDocument(computerGroupDocument, scrappyData)
        } else {
            val computer = Computer.newInstance(headline.childNodes().first().toString(), headline.absUrl("href"))
            println("${computer.name}\n\t${computer.url}")
            scrappyData.computers.add(computer)
            fetchPCInformation(computer, scrappyData)
        }
    }

    private fun fetchPCInformation(computer: Computer, scrappyData: ScrappyData) {
        val doc = Jsoup.connect(computer.url).get()
        computer.price = readPrice(doc)
        computer.specs.addAll(readAllSpecs(doc, computer, scrappyData))
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

    private fun readAllSpecs(doc: Document, computer: Computer, scrappyData: ScrappyData): Iterable<ComputerSpecificationAssignation> {
        val specs: MutableSet<ComputerSpecificationAssignation> = mutableSetOf()
        val specTables = doc.select(FULL_PRODUCT_SPECIFICATION_SELECTOR)

        // Pobierz ze wszystkich tabel specyfikacje
        for (specTable in specTables) {
            if (specTable.toString().contains("ng-")) continue

            for (tableSpecRow in specTable.select(".section table tr")) {
                val specification = getAndAddSpec(tableSpecRow, scrappyData)
                val value = getAndAddSpecValues(tableSpecRow, specification, scrappyData)
                specs.add(ComputerSpecificationAssignation.newInstance(computer, specification, value))
            }
        }
        return specs
    }

    private fun getAndAddSpec(tableSpecRow: Element, scrappyData: ScrappyData): ComputerSpecification {
        val specName = tableSpecRow.select("th").text().trim()
        val specification = ComputerSpecification.newInstance(specName)

        scrappyData.properties.add(specification)
        return specification
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
                if (value.isNotEmpty())
                    specValues.add(ComputerSpecificationValue.newInstance(specification, value))

        } else specValues.add(ComputerSpecificationValue.newInstance(specification, specValuesCell.text()))

        if (scrappyData.propertiesPossibleValues.containsKey(specification))
            scrappyData.propertiesPossibleValues[specification] = specValues
        else scrappyData.propertiesPossibleValues[specification]?.addAll(specValues)

        return specValues
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
