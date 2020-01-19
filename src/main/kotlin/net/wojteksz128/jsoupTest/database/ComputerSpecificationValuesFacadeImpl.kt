package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.dao.ComputerSpecificationValues
import net.wojteksz128.jsoupTest.dao.ComputerSpecifications
import net.wojteksz128.jsoupTest.dao.Computers
import net.wojteksz128.jsoupTest.model.ComputerSpecification
import net.wojteksz128.jsoupTest.model.ComputerSpecificationValue
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ComputerSpecificationValuesFacadeImpl : ComputerSpecificationValuesFacade {
    override lateinit var appDatabase: AppDatabase

    override fun getAllBySpecification(specification: ComputerSpecification): Iterable<ComputerSpecificationValue> =
        transaction(appDatabase.database) {
            ComputerSpecificationValues.select { ComputerSpecificationValues.specificationId eq specification.id!! }
                .map { mapToFullObject(it, specification) }
        }

    override fun getById(id: Int): ComputerSpecificationValue = transaction(appDatabase.database) {
        ComputerSpecificationValues.select { ComputerSpecificationValues.id eq id }.map {
            mapToFullObject(
                it,
                appDatabase.specifications.getById(it[ComputerSpecificationValues.specificationId].value)
            )
        }.first()
    }

    override fun getAllByIds(ids: Iterable<Int>): Iterable<ComputerSpecificationValue> =
        transaction(appDatabase.database) {
            ComputerSpecificationValues.select { ComputerSpecificationValues.id inList ids }.map {
                mapToFullObject(
                    it,
                    appDatabase.specifications.getById(it[ComputerSpecificationValues.specificationId].value)
                )
            }
        }

    private fun mapToFullObject(it: ResultRow, specification: ComputerSpecification) = ComputerSpecificationValue(
        it[ComputerSpecificationValues.id].value,
        specification,
        it[ComputerSpecificationValues.name]
    )

    override fun save(obj: ComputerSpecificationValue) = transaction(appDatabase.database) {
        check(obj.id == null) { "Computer specification value probably already inserted (have id)." }

        val specificationValueId = ComputerSpecificationValues.insert {
            it[specificationId] = EntityID(obj.specification.id, ComputerSpecifications)
            it[name] = obj.name
        } get ComputerSpecificationValues.id
        obj.id = specificationValueId.value
    }

    override fun update(obj: ComputerSpecificationValue) = transaction(appDatabase.database) {
        check(obj.id != null) { "Computer specification value probably never inserted (not have id)" }

        val updatedRecordsNo = ComputerSpecificationValues.update({ Computers.id eq obj.id!! }) {
            it[specificationId] = EntityID(obj.specification.id, ComputerSpecifications)
            it[name] = obj.name
        }

        check(updatedRecordsNo == 1) { "Computer specification value with id=${obj.id} not found" }
    }

    override fun delete(obj: ComputerSpecificationValue) = transaction(appDatabase.database) {
        check(obj.id != null) { "Computer specification value probably never inserted (not have id)" }

        val deletedRecordsNo = Computers.deleteWhere { Computers.id eq obj.id!! }
        check(deletedRecordsNo == 1) { "Computer specification value with id=${obj.id} not found" }
    }

    override fun saveIfNotExist(obj: ComputerSpecificationValue) = transaction(appDatabase.database) {
        val isExistingObj = checkIfExist(obj)
        if(isExistingObj) {
            val specificationValueId = getSpecValByName(obj).id
            obj.id = specificationValueId
        }
        else{
            save(obj)
        }
    }

    private fun checkIfExist(obj: ComputerSpecificationValue) = transaction(appDatabase.database) {
        ComputerSpecificationValues.select { ComputerSpecificationValues.name eq obj.name }.any()
    }

    private fun getSpecValByName(obj: ComputerSpecificationValue)  = transaction(appDatabase.database) {
        ComputerSpecificationValues.select { ComputerSpecificationValues.name eq obj.name  }.map {
            mapToFullObject(
            it,
            appDatabase.specifications.getById(it[ComputerSpecificationValues.specificationId].value)
        ) }.first()
    }

}
