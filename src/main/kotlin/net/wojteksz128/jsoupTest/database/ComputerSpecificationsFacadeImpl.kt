package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.dao.ComputerSpecifications
import net.wojteksz128.jsoupTest.model.ComputerSpecification
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ComputerSpecificationsFacadeImpl(
    private val databaseConnection: Database,
    private val specificationValuesFacade: ComputerSpecificationValuesFacade
) : ComputerSpecificationsFacade {
    init {
        transaction(databaseConnection) {
            SchemaUtils.create(ComputerSpecifications)
        }
    }

    override fun getAllWithPotentialValues(): Iterable<ComputerSpecification> = transaction(databaseConnection) {
        val specifications = ComputerSpecifications.selectAll()
            .map {
                ComputerSpecification(
                    it[ComputerSpecifications.id],
                    it[ComputerSpecifications.name],
                    null
                )
            }

        specifications.forEach { it.values = specificationValuesFacade.getAllBySpecification(it).toSet() }

        return@transaction specifications.toSet()
    }

    override fun save(obj: ComputerSpecification) = transaction(databaseConnection) {
        check(obj.id == null) { "Computer specification probably already inserted (have id)." }

        val specificationId = ComputerSpecifications.insert { it[name] = obj.name } get ComputerSpecifications.id
        obj.id = specificationId
    }

    override fun save(collection: Iterable<ComputerSpecification>) = transaction {
        collection.forEach { save(it) }
    }

    override fun update(obj: ComputerSpecification) {
        check(obj.id != null) { "Computer specification probably never inserted (not have id)" }

        val updatedRecordsNo =
            ComputerSpecifications.update({ ComputerSpecifications.id eq obj.id!! }) { it[name] = obj.name }

        check(updatedRecordsNo == 1) { "Computer specification with id=${obj.id} not found" }
    }

    override fun update(collection: Iterable<ComputerSpecification>) = transaction {
        collection.forEach { update(it) }
    }

    override fun delete(obj: ComputerSpecification) = transaction(databaseConnection) {
        check(obj.id != null) { "Computer specification probably never inserted (not have id)" }

        val deletedRecordsNo = ComputerSpecifications.deleteWhere { ComputerSpecifications.id eq obj.id!! }
        check(deletedRecordsNo == 1) { "Computer specification with id=${obj.id} not found" }
    }

    override fun delete(collection: Iterable<ComputerSpecification>) = transaction {
        collection.forEach { delete(it) }
    }

    override fun close() {
    }
}