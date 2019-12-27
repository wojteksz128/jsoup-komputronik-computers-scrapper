package net.wojteksz128.jsoupTest.dao

import org.jetbrains.exposed.sql.Table

object Computers : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 50)
    val url = varchar("url", 100)
    val price = integer("price")
}