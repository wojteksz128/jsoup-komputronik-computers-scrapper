package net.wojteksz128.jsoupTest.dao

import org.jetbrains.exposed.sql.Table

object ScrapInstances : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val createDate = datetime("createDate")
    val endDate = datetime("endDate")
    val computersNo = integer("computersNo")
}