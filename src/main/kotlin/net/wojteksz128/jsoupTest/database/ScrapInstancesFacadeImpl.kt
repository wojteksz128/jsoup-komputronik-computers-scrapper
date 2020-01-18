package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.dao.ScrapInstances
import net.wojteksz128.jsoupTest.model.ScrapInstance
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ScrapInstancesFacadeImpl : ScrapInstancesFacade {
    override lateinit var appDatabase: AppDatabase

    override fun getById(id: Int) = transaction(appDatabase.database) {
        ScrapInstances.select { ScrapInstances.id eq id }.map { mapToFullObject(it) }.first()
    }

    override fun getAllByIds(ids: Iterable<Int>) = transaction(appDatabase.database) {
        ScrapInstances.select { ScrapInstances.id inList ids }.map { mapToFullObject(it) }
    }

    override fun getLastScrapInstance(): ScrapInstance = transaction(appDatabase.database) {
        ScrapInstances.selectAll().orderBy(ScrapInstances.createDate, SortOrder.DESC).map { mapToFullObject(it) }
            .first()
    }

    private fun mapToFullObject(it: ResultRow) =
        ScrapInstance(it[ScrapInstances.id].value, it[ScrapInstances.createDate], it[ScrapInstances.endDate])

    override fun save(obj: ScrapInstance) = transaction(appDatabase.database) {
        check(obj.id == null) { "Computer probably already inserted (have id)." }

        val scrapInstanceId = ScrapInstances.insert {
            it[createDate] = obj.createDate
            it[endDate] = obj.endDate!!
            it[computersNo] = obj.computers.size
        } get ScrapInstances.id
        obj.id = scrapInstanceId.value
    }

    override fun update(obj: ScrapInstance) = transaction(appDatabase.database) {
        check(obj.id != null) { "Scrap instance probably never inserted (not have id)" }

        val updatedRecordsNo = ScrapInstances.update({ ScrapInstances.id eq obj.id!! }) {
            it[createDate] = obj.createDate
            it[endDate] = obj.endDate!!
            it[computersNo] = obj.computers.size
        }

        check(updatedRecordsNo == 1) { "Computer with id=${obj.id} not found" }
    }

    override fun delete(obj: ScrapInstance) = transaction(appDatabase.database) {
        check(obj.id != null) { "Scrap instance probably never inserted (not have id)" }

        val deletedRecordsNo = ScrapInstances.deleteWhere { ScrapInstances.id eq obj.id!! }
        check(deletedRecordsNo == 1) { "Scrap instance with id=${obj.id} not found" }
    }

    override fun saveIfNotExist(obj: ScrapInstance) = transaction(appDatabase.database) {
        save(obj)
    }
}
