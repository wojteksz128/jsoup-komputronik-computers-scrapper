package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.dao.ComputerSpecificationValuesAssignations
import net.wojteksz128.jsoupTest.model.ComputerSpecificationAssignation
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ComputerSpecificationAssignationFacadeImpl(private val databaseConnection: Database) :
    ComputerSpecificationAssignationFacade {
    init {
        transaction(databaseConnection) {
            SchemaUtils.create(ComputerSpecificationValuesAssignations)
        }
    }

    override fun save(obj: ComputerSpecificationAssignation) = transaction(databaseConnection) {
        check(obj.id == null) { "Computer specification assignation probably already inserted (have id)." }

        val specificationAssignationId = ComputerSpecificationValuesAssignations.insert {
            it[computerId] = obj.computer.id!!
            it[valueId] = obj.value.id!!
        } get ComputerSpecificationValuesAssignations.id
        obj.id = specificationAssignationId
    }

    override fun save(collection: Iterable<ComputerSpecificationAssignation>) = transaction {
        collection.forEach { save(it) }
    }

    override fun update(obj: ComputerSpecificationAssignation) = transaction(databaseConnection) {
        check(obj.id != null) { "Computer specification assignation probably never inserted (not have id)" }

        val updatedRecordsNo =
            ComputerSpecificationValuesAssignations.update({ ComputerSpecificationValuesAssignations.id eq obj.id!! }) {
                it[computerId] = obj.computer.id!!
                it[valueId] = obj.value.id!!
            }

        check(updatedRecordsNo == 1) { "Computer specification assignation with id=${obj.id} not found" }
    }

    override fun update(collection: Iterable<ComputerSpecificationAssignation>) = transaction {
        collection.forEach { update(it) }
    }

    override fun delete(obj: ComputerSpecificationAssignation) = transaction(databaseConnection) {
        check(obj.id != null) { "Computer specification assignation probably never inserted (not have id)" }

        val deletedRecordsNo =
            ComputerSpecificationValuesAssignations.deleteWhere { ComputerSpecificationValuesAssignations.id eq obj.id!! }
        check(deletedRecordsNo == 1) { "Computer specification assignation with id=${obj.id} not found" }
    }

    override fun delete(collection: Iterable<ComputerSpecificationAssignation>) = transaction {
        collection.forEach { delete(it) }
    }

    override fun close() {
    }
}