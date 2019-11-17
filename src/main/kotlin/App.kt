import org.jsoup.Jsoup

fun main() {
    val doc = Jsoup.connect("https://www.komputronik.pl/category/5801/komputery-pc.html").get()
    println(doc.title())
    val newsHeadlines = doc.select("#products-list > div .pe2-head a.blank-link")
    for (headline in newsHeadlines) {
        println("${headline.childNodes().first()}\n\t${headline.absUrl("href")}")
    }
}