package net.wojteksz128.jsoupTest.dao

import org.jetbrains.exposed.dao.IntIdTable

object ScrapInstances : IntIdTable() {
    val createDate = datetime("createDate")
    val endDate = datetime("endDate")
    val computersNo = integer("computersNo")
}