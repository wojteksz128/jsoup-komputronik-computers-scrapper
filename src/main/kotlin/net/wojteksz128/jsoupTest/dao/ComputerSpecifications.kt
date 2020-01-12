package net.wojteksz128.jsoupTest.dao

import org.jetbrains.exposed.dao.IntIdTable

object ComputerSpecifications : IntIdTable() {
    val name = varchar("name", 50)
}

object ComputerSpecificationValues : IntIdTable() {
    val specificationId = integer("specificationId").entityId() references ComputerSpecifications.id
    val name = varchar("value", 128)
}

object ComputerSpecificationValuesAssignations : IntIdTable() {
    val computerId = integer("computerId").entityId() references Computers.id
    val valueId = integer("valueId").entityId() references ComputerSpecificationValues.id
}