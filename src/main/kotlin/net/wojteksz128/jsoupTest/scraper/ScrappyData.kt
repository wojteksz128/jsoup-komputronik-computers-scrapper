package net.wojteksz128.jsoupTest.scraper

import net.wojteksz128.jsoupTest.model.Computer
import net.wojteksz128.jsoupTest.model.ComputerSpecification
import net.wojteksz128.jsoupTest.model.ComputerSpecificationValue
import java.time.LocalDateTime

data class ScrappyData(
    val createDate: LocalDateTime,
    var endDate: LocalDateTime? = null,
    var computers: MutableSet<Computer> = mutableSetOf(),
    val properties: MutableSet<ComputerSpecification> = mutableSetOf(),
    val propertiesPossibleValues: MutableMap<ComputerSpecification, MutableSet<ComputerSpecificationValue>> = mutableMapOf()
) {
    fun addOrGetProperty(specification: ComputerSpecification): ComputerSpecification {
        return if (!properties.contains(specification)) {
            properties += specification
            specification
        } else
            properties.stream().filter { it == specification }.findFirst().orElseThrow { Exception() }
    }

    fun addOrGetPropertyValue(
        specification: ComputerSpecification,
        specificationValue: ComputerSpecificationValue
    ): ComputerSpecificationValue {
        if (propertiesPossibleValues.containsKey(specification).not())
            propertiesPossibleValues[specification] = mutableSetOf()

        val possibleSpecificationValues = propertiesPossibleValues[specification]!!
        if (specificationValue in possibleSpecificationValues)
            return possibleSpecificationValues.stream().filter { it == specificationValue }.findFirst()
                .orElseThrow { Exception() }
        else {
            possibleSpecificationValues += specificationValue
            return specificationValue
        }
    }
}