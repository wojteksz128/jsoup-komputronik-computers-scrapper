package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.dao.ComputerSpecificationValuesAssignations
import net.wojteksz128.jsoupTest.dao.Computers
import net.wojteksz128.jsoupTest.dao.ScrapInstances
import net.wojteksz128.jsoupTest.model.Computer
import net.wojteksz128.jsoupTest.model.ScrapInstance
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ComputersFacadeImpl : ComputersFacade {
    override lateinit var appDatabase: AppDatabase

    override fun getAll() = transaction(appDatabase.database) {
        Computers.selectAll()
            .map { mapToFullObject(it, appDatabase.scrapInstances.getById(it[Computers.scrapInstanceId].value)) }
    }

    override fun getAllForLastScrap() = transaction(appDatabase.database) {
        val lastScrapInstance = appDatabase.scrapInstances.getLastScrapInstance()

        Computers.select { Computers.scrapInstanceId eq lastScrapInstance.id!! }
            .map { mapToFullObject(it, lastScrapInstance) }
    }

    override fun getAllForLastScrapContainingValues(valuesIds: Iterable<Int>) = transaction(appDatabase.database) {
        val lastScrapInstance = appDatabase.scrapInstances.getLastScrapInstance()

        Computers.innerJoin(ComputerSpecificationValuesAssignations)
            .select { Computers.scrapInstanceId eq lastScrapInstance.id!! }
            .andWhere { ComputerSpecificationValuesAssignations.valueId inList valuesIds }
            .adjustSlice { slice(Computers.columns) }
            .withDistinct(true)
            .map { mapToFullObject(it, lastScrapInstance) }
    }

    override fun getById(id: Int) = transaction(appDatabase.database) {
        Computers.select { Computers.id eq id }
            .map { mapToFullObject(it, appDatabase.scrapInstances.getById(it[Computers.scrapInstanceId].value)) }
            .first()
    }

    override fun getAllByIds(ids: Iterable<Int>) = transaction(appDatabase.database) {
        Computers.select { Computers.id inList ids }
            .map { mapToFullObject(it, appDatabase.scrapInstances.getById(it[Computers.scrapInstanceId].value)) }
    }

    private fun mapToFullObject(it: ResultRow, scrapInstanceId: ScrapInstance) =
        Computer(it[Computers.id].value, it[Computers.name], it[Computers.url], it[Computers.price], scrapInstanceId)

    override fun save(obj: Computer) = transaction(appDatabase.database) {
        check(obj.id == null) { "Computer probably already inserted (have id)." }

        val computerId = Computers.insert {
            it[name] = obj.name
            it[price] = obj.price!!
            it[url] = obj.url
            it[scrapInstanceId] = EntityID(obj.scrapInstance.id, ScrapInstances)
        } get Computers.id
        obj.id = computerId.value
    }

    override fun update(obj: Computer) = transaction(appDatabase.database) {
        check(obj.id != null) { "Computer probably never inserted (not have id)" }

        val updatedRecordsNo = Computers.update({ Computers.id eq obj.id!! }) {
            it[name] = obj.name
            it[price] = obj.price!!
            it[url] = obj.url
        }

        check(updatedRecordsNo == 1) { "Computer with id=${obj.id} not found" }
    }

    override fun delete(obj: Computer) = transaction(appDatabase.database) {
        check(obj.id != null) { "Computer probably never inserted (not have id)" }

        val deletedRecordsNo = Computers.deleteWhere { Computers.id eq obj.id!! }
        check(deletedRecordsNo == 1) { "Computer with id=${obj.id} not found" }
    }

    override fun saveIfNotExist(obj: Computer) = transaction(appDatabase.database) {
        save(obj)
    }
}