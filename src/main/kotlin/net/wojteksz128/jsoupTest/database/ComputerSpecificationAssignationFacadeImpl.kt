package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.dao.ComputerSpecificationValues
import net.wojteksz128.jsoupTest.dao.ComputerSpecificationValuesAssignations
import net.wojteksz128.jsoupTest.dao.Computers
import net.wojteksz128.jsoupTest.model.Computer
import net.wojteksz128.jsoupTest.model.ComputerSpecificationAssignation
import net.wojteksz128.jsoupTest.model.ComputerSpecificationValue
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ComputerSpecificationAssignationFacadeImpl : ComputerSpecificationAssignationFacade {
    override lateinit var appDatabase: AppDatabase

    override fun getById(id: Int) = transaction(appDatabase.database) {
        ComputerSpecificationValuesAssignations.select { ComputerSpecificationValuesAssignations.id eq id }.map {
            mapToFullObject(
                it,
                appDatabase.computers.getById(it[ComputerSpecificationValuesAssignations.computerId].value),
                appDatabase.specificationsValues.getById(it[ComputerSpecificationValuesAssignations.valueId].value)
            )
        }.first()
    }

    override fun getAllByIds(ids: Iterable<Int>) = transaction(appDatabase.database) {
        ComputerSpecificationValuesAssignations.select { ComputerSpecificationValuesAssignations.id inList ids }.map {
            mapToFullObject(
                it,
                appDatabase.computers.getById(it[ComputerSpecificationValuesAssignations.computerId].value),
                appDatabase.specificationsValues.getById(it[ComputerSpecificationValuesAssignations.valueId].value)
            )
        }
    }

    private fun mapToFullObject(
        it: ResultRow,
        computer: Computer,
        value: ComputerSpecificationValue
    ) = ComputerSpecificationAssignation(
        it[ComputerSpecificationValuesAssignations.id].value,
        computer,
        value.specification,
        value
    )

    override fun save(obj: ComputerSpecificationAssignation) = transaction(appDatabase.database) {
        check(obj.id == null) { "Computer specification assignation probably already inserted (have id)." }

        val specificationAssignationId = ComputerSpecificationValuesAssignations.insert {
            it[computerId] = EntityID(obj.computer.id, Computers)
            it[valueId] = EntityID(obj.value.id, ComputerSpecificationValues)
        } get ComputerSpecificationValuesAssignations.id
        obj.id = specificationAssignationId.value
    }

    override fun update(obj: ComputerSpecificationAssignation) = transaction(appDatabase.database) {
        check(obj.id != null) { "Computer specification assignation probably never inserted (not have id)" }

        val updatedRecordsNo =
            ComputerSpecificationValuesAssignations.update({ ComputerSpecificationValuesAssignations.id eq obj.id!! }) {
                it[computerId] = EntityID(obj.computer.id, Computers)
                it[valueId] = EntityID(obj.value.id, ComputerSpecificationValues)
            }

        check(updatedRecordsNo == 1) { "Computer specification assignation with id=${obj.id} not found" }
    }

    override fun delete(obj: ComputerSpecificationAssignation) = transaction(appDatabase.database) {
        check(obj.id != null) { "Computer specification assignation probably never inserted (not have id)" }

        val deletedRecordsNo =
            ComputerSpecificationValuesAssignations.deleteWhere { ComputerSpecificationValuesAssignations.id eq obj.id!! }
        check(deletedRecordsNo == 1) { "Computer specification assignation with id=${obj.id} not found" }
    }

    override fun saveIfNotExist(obj: ComputerSpecificationAssignation) = transaction(appDatabase.database) {
        save(obj)
    }
}