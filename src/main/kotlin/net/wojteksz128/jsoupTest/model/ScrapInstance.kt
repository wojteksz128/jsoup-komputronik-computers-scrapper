package net.wojteksz128.jsoupTest.model

import org.joda.time.DateTime

data class ScrapInstance(
    var id: Int?,
    val createDate: DateTime,
    var endDate: DateTime?,
    val computers: MutableSet<Computer>
) {
    constructor(createDate: DateTime) : this(null, createDate, null, mutableSetOf())

    constructor(id: Int, createDate: DateTime, endDate: DateTime)
            : this(id, createDate, endDate, mutableSetOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScrapInstance

        if (id != other.id) return false
        if (createDate != other.createDate) return false
        if (endDate != other.endDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + createDate.hashCode()
        result = 31 * result + (endDate?.hashCode() ?: 0)
        return result
    }


}