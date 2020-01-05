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
)