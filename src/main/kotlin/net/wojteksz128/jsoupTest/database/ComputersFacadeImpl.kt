package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.dao.Computers
import net.wojteksz128.jsoupTest.model.Computer
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ComputersFacadeImpl(private val databaseConnection: Database) : ComputersFacade {
    init {
        transaction(databaseConnection) {
            SchemaUtils.create(Computers)
        }
    }

    override fun getAll(): Iterable<Computer> = transaction(databaseConnection) {
        Computers.selectAll()
            .map { Computer.newInstance(it[Computers.id], it[Computers.name], it[Computers.url], it[Computers.price]) }
    }

    override fun save(obj: Computer) = transaction(databaseConnection) {
        check(obj.id == null) { "Computer probably already inserted (have id)." }

        val computerId = Computers.insert {
            it[name] = obj.name
            it[price] = obj.price!!
            it[url] = obj.url
        } get Computers.id
        obj.id = computerId
    }

    override fun save(collection: Iterable<Computer>) = transaction {
        collection.forEach { save(it) }
    }

    override fun update(obj: Computer) = transaction(databaseConnection) {
        check(obj.id != null) { "Computer probably never inserted (not have id)" }

        val updatedRecordsNo = Computers.update({ Computers.id eq obj.id!! }) {
            it[name] = obj.name
            it[price] = obj.price!!
            it[url] = obj.url
        }

        check(updatedRecordsNo == 1) { "Computer with id=${obj.id} not found" }
    }

    override fun update(collection: Iterable<Computer>) = transaction {
        collection.forEach { update(it) }
    }

    override fun delete(obj: Computer) = transaction(databaseConnection) {
        check(obj.id != null) { "Computer probably never inserted (not have id)" }

        val deletedRecordsNo = Computers.deleteWhere { Computers.id eq obj.id!! }
        check(deletedRecordsNo == 1) { "Computer with id=${obj.id} not found" }
    }

    override fun delete(collection: Iterable<Computer>) = transaction {
        collection.forEach { delete(it) }
    }

    override fun close() {
    }
}