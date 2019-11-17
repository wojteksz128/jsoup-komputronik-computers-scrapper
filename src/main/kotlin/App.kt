import org.jsoup.Jsoup
import org.jsoup.select.Elements

fun main() {
    val doc = Jsoup.connect("https://www.komputronik.pl/category/5801/komputery-pc.html").get()
    println(doc.title()+"\n")
    val newsHeadlines = doc.select("#products-list > div .pe2-head a.blank-link")

    fetchPcList(newsHeadlines)
}

fun fetchPcList(newsHeadlines:Elements)
{
    for (headline in newsHeadlines) {
        if (!headline.childNodes().first().toString().startsWith(" {{")) {
            if (headline.absUrl("href").contains("productVariantGroup")){
                val doc2 = Jsoup.connect(headline.absUrl("href")).get()
                val newsHeadlines2 = doc2.select("#products-list > div .pe2-head a.blank-link")

                fetchPcList(newsHeadlines2)
            }
            else {
                println("${headline.childNodes().first()}\n\t${headline.absUrl("href")}")
            }
        }
    }
}