package net.wojteksz128.jsoupTest.scraper

import net.wojteksz128.jsoupTest.model.ComputerSpecification
import net.wojteksz128.jsoupTest.model.ComputerSpecificationValue
import net.wojteksz128.jsoupTest.model.ScrapInstance
import org.joda.time.DateTime

data class ScrappyData(
    val scrapInstance: ScrapInstance,
    val properties: MutableSet<ComputerSpecification>,
    val propertiesPossibleValues: MutableMap<ComputerSpecification, MutableSet<ComputerSpecificationValue>>
) {

    constructor(createDate: DateTime)
            : this(ScrapInstance(createDate), mutableSetOf(), mutableMapOf())

    fun addOrGetProperty(specification: ComputerSpecification): ComputerSpecification {
        synchronized(properties) {
            return if (specification in properties)
                properties.stream().filter { it == specification }.findFirst().orElseThrow { Exception() }
            else {
                synchronized(properties) {
                    properties += specification
                }
                specification
            }
        }
    }

    fun addOrGetPropertyValue(
        specification: ComputerSpecification,
        specificationValue: ComputerSpecificationValue
    ): ComputerSpecificationValue {
        synchronized(propertiesPossibleValues) {
            if (propertiesPossibleValues.containsKey(specification).not())
                propertiesPossibleValues[specification] = mutableSetOf()

            val possibleSpecificationValues = propertiesPossibleValues[specification]!!
            return if (specificationValue in possibleSpecificationValues)
                possibleSpecificationValues.stream().filter { it == specificationValue }.findFirst()
                    .orElseThrow { Exception() }
            else {
                possibleSpecificationValues += specificationValue
                specificationValue
            }
        }
    }
}