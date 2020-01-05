package net.wojteksz128.jsoupTest.model

data class Computer(
    var id: Int?,
    var name: String,
    var url: String,
    var price: Double?,
    var specs: MutableSet<ComputerSpecificationAssignation>
) {



    companion object {
        fun newInstance(name: String, url: String) = Computer(null, name, url, null, mutableSetOf())
        fun newInstance(id: Int, name: String, url: String, price: Double) =
            Computer(id, name, url, price, mutableSetOf())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Computer

        if (id != other.id) return false
        if (name != other.name) return false
        if (url != other.url) return false
        if (price != other.price) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + (price?.hashCode() ?: 0)
        return result
    }
}