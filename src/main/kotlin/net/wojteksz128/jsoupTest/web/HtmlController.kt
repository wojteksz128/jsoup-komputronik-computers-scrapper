package net.wojteksz128.jsoupTest.web

import net.wojteksz128.jsoupTest.dao.DAOFacadePc
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import java.text.NumberFormat

@Controller
class HtmlController(private val dao: DAOFacadePc) {

    @GetMapping("/")
    fun home(model: Model): String {
        model["pcs"] = dao.getAllComputers()
        val function = { price: Int -> NumberFormat.getCurrencyInstance().format(price) }
        model["currencyFormatter"] = function
        return "home"
    }
}