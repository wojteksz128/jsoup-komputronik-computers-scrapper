package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.dao.ComputerSpecifications
import net.wojteksz128.jsoupTest.model.ComputerSpecification
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ComputerSpecificationsFacadeImpl : ComputerSpecificationsFacade {
    override lateinit var appDatabase: AppDatabase

    override fun getAllWithPotentialValues() = transaction(appDatabase.database) {
        val specifications = ComputerSpecifications.selectAll()
            .map { mapToFullObject(it) }

        specifications.forEach { it.values = appDatabase.specificationsValues.getAllBySpecification(it).toSet() }

        return@transaction specifications
    }

    override fun getById(id: Int) = transaction(appDatabase.database) {
        ComputerSpecifications.select { ComputerSpecifications.id eq id }.map { mapToFullObject(it) }.first()
    }

    override fun getAllByIds(ids: Iterable<Int>) = transaction(appDatabase.database) {
        ComputerSpecifications.select { ComputerSpecifications.id inList ids }.map { mapToFullObject(it) }
    }

    private fun mapToFullObject(it: ResultRow) =
        ComputerSpecification(it[ComputerSpecifications.id].value, it[ComputerSpecifications.name], null)

    override fun save(obj: ComputerSpecification) = transaction(appDatabase.database) {
        check(obj.id == null) { "Computer specification probably already inserted (have id)." }

        val specificationId = ComputerSpecifications.insert { it[name] = obj.name } get ComputerSpecifications.id
        obj.id = specificationId.value
    }

    override fun update(obj: ComputerSpecification) {
        check(obj.id != null) { "Computer specification probably never inserted (not have id)" }

        val updatedRecordsNo =
            ComputerSpecifications.update({ ComputerSpecifications.id eq obj.id!! }) { it[name] = obj.name }

        check(updatedRecordsNo == 1) { "Computer specification with id=${obj.id} not found" }
    }

    override fun delete(obj: ComputerSpecification) = transaction(appDatabase.database) {
        check(obj.id != null) { "Computer specification probably never inserted (not have id)" }

        val deletedRecordsNo = ComputerSpecifications.deleteWhere { ComputerSpecifications.id eq obj.id!! }
        check(deletedRecordsNo == 1) { "Computer specification with id=${obj.id} not found" }
    }

    override fun saveIfNotExist(obj: ComputerSpecification) = transaction(appDatabase.database) {

        val existingId = getIdByValue(obj.name)?.id
        if(existingId == null) {
            save(obj)
        }
        else{
            obj.id = existingId
        }
    }

    private fun getIdByValue(value: String) = transaction(appDatabase.database) {
        ComputerSpecifications.select { ComputerSpecifications.name eq value }.map { mapToFullObject(it) }.singleOrNull()
    }
}