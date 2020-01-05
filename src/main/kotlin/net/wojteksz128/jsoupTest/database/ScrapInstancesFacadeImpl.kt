package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.dao.ScrapInstances
import net.wojteksz128.jsoupTest.model.ScrapInstance
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ScrapInstancesFacadeImpl(private val databaseConnection: Database) : ScrapInstancesFacade {
    override fun getById(id: Int): ScrapInstance = transaction(databaseConnection) {
        ScrapInstances.select { ScrapInstances.id eq id }
            .map { ScrapInstance(it[ScrapInstances.id], it[ScrapInstances.createDate], it[ScrapInstances.endDate]) }
            .first()
    }

    override fun save(obj: ScrapInstance) = transaction(databaseConnection) {
        check(obj.id == null) { "Computer probably already inserted (have id)." }

        val scrapInstanceId = ScrapInstances.insert {
            it[createDate] = obj.createDate
            it[endDate] = obj.endDate!!
            it[computersNo] = obj.computers.size
        } get ScrapInstances.id
        obj.id = scrapInstanceId
    }

    override fun save(collection: Iterable<ScrapInstance>) = transaction {
        collection.forEach { save(it) }
    }

    override fun update(obj: ScrapInstance) {
        check(obj.id != null) { "Scrap instance probably never inserted (not have id)" }

        val updatedRecordsNo = ScrapInstances.update({ ScrapInstances.id eq obj.id!! }) {
            it[createDate] = obj.createDate
            it[endDate] = obj.endDate!!
            it[computersNo] = obj.computers.size
        }

        check(updatedRecordsNo == 1) { "Computer with id=${obj.id} not found" }
    }

    override fun update(collection: Iterable<ScrapInstance>) {
        collection.forEach { update(it) }
    }

    override fun delete(obj: ScrapInstance) {
        check(obj.id != null) { "Scrap instance probably never inserted (not have id)" }

        val deletedRecordsNo = ScrapInstances.deleteWhere { ScrapInstances.id eq obj.id!! }
        check(deletedRecordsNo == 1) { "Scrap instance with id=${obj.id} not found" }
    }

    override fun delete(collection: Iterable<ScrapInstance>) {
        collection.forEach { delete(it) }
    }

    override fun close() {
    }

}
