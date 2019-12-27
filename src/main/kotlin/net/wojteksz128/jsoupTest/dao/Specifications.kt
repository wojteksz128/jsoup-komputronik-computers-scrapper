package net.wojteksz128.jsoupTest.dao

import org.jetbrains.exposed.sql.Table

object Specifications : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val attributeName = varchar("attributeName", 50)
    val attributeValue = varchar("value", 100)
    val pcId = (integer("pcId") references Computers.id)
}