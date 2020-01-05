package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.dao.ComputerSpecificationValues
import net.wojteksz128.jsoupTest.dao.Computers
import net.wojteksz128.jsoupTest.model.ComputerSpecificationValue
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ComputerSpecificationValuesFacadeImpl(private val databaseConnection: Database) :
    ComputerSpecificationValuesFacade {
    init {
        transaction(databaseConnection) {
            SchemaUtils.create(ComputerSpecificationValues)
        }
    }

    override fun save(obj: ComputerSpecificationValue) = transaction(databaseConnection) {
        check(obj.id == null) { "Computer specification value probably already inserted (have id)." }

        val specificationValueId = ComputerSpecificationValues.insert {
            it[specificationId] = obj.specification.id!!
            it[name] = obj.name
        } get ComputerSpecificationValues.id
        obj.id = specificationValueId
    }

    override fun save(collection: Iterable<ComputerSpecificationValue>) = transaction {
        collection.forEach { save(it) }
    }

    override fun update(obj: ComputerSpecificationValue) = transaction(databaseConnection) {
        check(obj.id != null) { "Computer specification value probably never inserted (not have id)" }

        val updatedRecordsNo = ComputerSpecificationValues.update({ Computers.id eq obj.id!! }) {
            it[specificationId] = obj.specification.id!!
            it[name] = obj.name
        }

        check(updatedRecordsNo == 1) { "Computer specification value with id=${obj.id} not found" }
    }

    override fun update(collection: Iterable<ComputerSpecificationValue>) = transaction {
        collection.forEach { update(it) }
    }

    override fun delete(obj: ComputerSpecificationValue) = transaction(databaseConnection) {
        check(obj.id != null) { "Computer specification value probably never inserted (not have id)" }

        val deletedRecordsNo = Computers.deleteWhere { Computers.id eq obj.id!! }
        check(deletedRecordsNo == 1) { "Computer specification value with id=${obj.id} not found" }
    }

    override fun delete(collection: Iterable<ComputerSpecificationValue>) = transaction {
        collection.forEach { delete(it) }
    }

    override fun close() {
    }
}