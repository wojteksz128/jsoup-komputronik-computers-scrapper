package net.wojteksz128.jsoupTest.web

import net.wojteksz128.jsoupTest.database.AppDatabase
import net.wojteksz128.jsoupTest.model.Computer
import net.wojteksz128.jsoupTest.model.ComputerSpecification
import net.wojteksz128.jsoupTest.model.ComputerSpecificationValue
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.text.NumberFormat
import kotlin.streams.toList

@Controller
class HtmlController(private val appDatabase: AppDatabase) {

    @GetMapping("/")
    fun home(model: Model, @RequestParam(required = false, name = "specValue") valuesIds: List<Int>?): String {
        val specValues = appDatabase.specificationsValues.getAllByIds(valuesIds ?: listOf())
            .groupBy({ it.specification.id!! }) { it.id!! }
        val filteredComputers = findComputers(specValues)
        val specifications = getAndSortSpecifications()
        val currencyFormatter = { price: Int -> NumberFormat.getCurrencyInstance().format(price) }

        model["pcs"] = filteredComputers
        model["filters"] = specifications
        model["selectedFilters"] = specValues
        model["currencyFormatter"] = currencyFormatter

        return "home"
    }

    private fun findComputers(specValues: Map<Int, List<Int>>): Iterable<Computer> {
        val specificationAssignedComputers =
            specValues.values.map { appDatabase.computers.getAllForLastScrapContainingValues(it) }
        var filteredComputers: Iterable<Computer>? = null

        specificationAssignedComputers.forEach { computers ->
            filteredComputers = filteredComputers?.let { it intersect computers } ?: computers
        }

        return filteredComputers ?: appDatabase.computers.getAllForLastScrap()
    }

    private fun getAndSortSpecifications(): Iterable<ComputerSpecification> {
        val specifications = appDatabase.specifications.getAllWithPotentialValues()
        specifications.forEach { spec ->
            spec.values =
                spec.values?.stream()?.sorted(Comparator.comparing(ComputerSpecificationValue::name))?.toList()?.toSet()
                    ?: setOf()
        }
        return specifications
    }
}