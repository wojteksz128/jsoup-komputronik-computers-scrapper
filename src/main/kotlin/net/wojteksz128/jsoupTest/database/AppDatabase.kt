package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.dao.*
import net.wojteksz128.jsoupTest.masterLabel
import net.wojteksz128.jsoupTest.scraper.ScrappyData
import net.wojteksz128.jsoupTest.stepLabel
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.stream.Collectors

class AppDatabase(
    val database: Database,
    val computers: ComputersFacade,
    val scrapInstances: ScrapInstancesFacade,
    val specifications: ComputerSpecificationsFacade,
    val specificationsValues: ComputerSpecificationValuesFacade,
    val specificationsAssignations: ComputerSpecificationAssignationFacade
) {
    init {
        computers.appDatabase = this
        scrapInstances.appDatabase = this
        specifications.appDatabase = this
        specificationsValues.appDatabase = this
        specificationsAssignations.appDatabase = this

        transaction(database) {
            SchemaUtils.create(
                Computers,
                ScrapInstances,
                ComputerSpecifications,
                ComputerSpecificationValues,
                ComputerSpecificationValuesAssignations
            )
        }
    }

    fun store(scrappyData: ScrappyData) {
        println("Store scrapped data into database".masterLabel())
        println("Store scrap instance".stepLabel())
        scrapInstances.save(scrappyData.scrapInstance)
        println("Store specifications".stepLabel())
        specifications.saveIfNotExist(scrappyData.properties)
        println("Store specifications values".stepLabel())
        specificationsValues.saveIfNotExist(scrappyData.propertiesPossibleValues.values.flatten())
        println("Store computers".stepLabel())
        computers.save(scrappyData.scrapInstance.computers)
        val computerSpecificationsAssignations =
            scrappyData.scrapInstance.computers.stream().flatMap { it.specs.stream() }.collect(Collectors.toSet())
        println("Store specifications assignations".stepLabel())
        specificationsAssignations.save(computerSpecificationsAssignations)
        println("End store scrapped data into database".masterLabel())
    }
}