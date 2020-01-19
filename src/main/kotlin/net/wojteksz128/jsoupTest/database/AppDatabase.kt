package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.PageGetter
import net.wojteksz128.jsoupTest.dao.*
import net.wojteksz128.jsoupTest.masterLabel
import net.wojteksz128.jsoupTest.scraper.ScrappyData
import net.wojteksz128.jsoupTest.stepLabel
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.stream.Collectors

class AppDatabase(
    val database: Database,
    val computers: ComputersFacade,
    val scrapInstances: ScrapInstancesFacade,
    val specifications: ComputerSpecificationsFacade,
    val specificationsValues: ComputerSpecificationValuesFacade,
    val specificationsAssignations: ComputerSpecificationAssignationFacade
) {
    private val log: Logger = LoggerFactory.getLogger(PageGetter::class.java)

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
        log.info("Store scrapped data into database".masterLabel())
        log.info("Store scrap instance".stepLabel())
        scrapInstances.save(scrappyData.scrapInstance)
        log.info("Store specifications".stepLabel())
        specifications.saveIfNotExist(scrappyData.properties)
        log.info("Store specifications values".stepLabel())
        specificationsValues.saveIfNotExist(scrappyData.propertiesPossibleValues.values.flatten())
        log.info("Store computers".stepLabel())
        computers.save(scrappyData.scrapInstance.computers)
        val computerSpecificationsAssignations =
            scrappyData.scrapInstance.computers.stream().flatMap { it.specs.stream() }.collect(Collectors.toSet())
        log.info("Store specifications assignations".stepLabel())
        specificationsAssignations.save(computerSpecificationsAssignations)
        log.info("End store scrapped data into database".masterLabel())
    }
}