package net.wojteksz128.jsoupTest.dao

import org.jetbrains.exposed.sql.Table

object ComputerSpecifications : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 50)
}

object ComputerSpecificationValues : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val specificationId = integer("specificationId") references ComputerSpecifications.id
    val name = varchar("value", 128)
}

object ComputerSpecificationValuesAssignations : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val computerId = integer("computerId") references Computers.id
    val valueId = integer("valueId") references ComputerSpecificationValues.id
}