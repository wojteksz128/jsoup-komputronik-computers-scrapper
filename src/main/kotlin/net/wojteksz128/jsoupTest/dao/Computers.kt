package net.wojteksz128.jsoupTest.dao

import org.jetbrains.exposed.dao.IntIdTable

object Computers : IntIdTable() {
    val name = varchar("name", 100)
    val url = varchar("url", 100)
    val price = double("price")
    val scrapInstanceId = integer("scrapInstanceId").entityId() references ScrapInstances.id
}