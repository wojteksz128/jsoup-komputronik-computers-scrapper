package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.scraper.ScrappyData
import org.jetbrains.exposed.sql.Database
import java.util.stream.Collectors

class AppDatabase(
    val database: Database,
    val computers: ComputersFacade,
    val specifications: ComputerSpecificationsFacade,
    val specificationsValues: ComputerSpecificationValuesFacade,
    val specificationsAssignations: ComputerSpecificationAssignationFacade
) {

    fun store(scrappyData: ScrappyData) {
        specifications.save(scrappyData.properties)
        specificationsValues.save(scrappyData.propertiesPossibleValues.values.flatten())
        computers.save(scrappyData.computers)
        val computerSpecificationsAssignations =
            scrappyData.computers.stream().flatMap { it.specs.stream() }.collect(Collectors.toSet())
        specificationsAssignations.save(computerSpecificationsAssignations)
    }
}