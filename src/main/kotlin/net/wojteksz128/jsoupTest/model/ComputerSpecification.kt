package net.wojteksz128.jsoupTest.model


data class ComputerSpecificationAssignation(
    var id: Int?,
    val computer: Computer,
    val specification: ComputerSpecification,
    val value: ComputerSpecificationValue
) {
    constructor(computer: Computer, specification: ComputerSpecification, value: ComputerSpecificationValue)
            : this(null, computer, specification, value)
}

data class ComputerSpecification(var id: Int?, val name: String, var values: Set<ComputerSpecificationValue>?) {
    constructor(name: String) : this(null, name, null)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ComputerSpecification

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + name.hashCode()
        return result
    }
}

data class ComputerSpecificationValue(var id: Int?, val specification: ComputerSpecification, val name: String) {
    constructor(specification: ComputerSpecification, name: String) : this(null, specification, name)
}
