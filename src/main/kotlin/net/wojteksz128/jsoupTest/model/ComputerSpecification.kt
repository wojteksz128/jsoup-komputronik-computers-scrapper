package net.wojteksz128.jsoupTest.model


data class PcSpecDto(val attributeName: String, val attributeValue: String, val pcId: Int)

data class ComputerSpecificationAssignation(
    var id: Int?,
    val computer: Computer,
    val specification: ComputerSpecification,
    val value: ComputerSpecificationValue
) {
    companion object {
        fun newInstance(
            computer: Computer,
            specification: ComputerSpecification,
            value: ComputerSpecificationValue
        ) = ComputerSpecificationAssignation(null, computer, specification, value)
    }
}

data class ComputerSpecification(var id: Int?, val name: String) {
    companion object {
        fun newInstance(name: String) = ComputerSpecification(null, name)
    }
}

data class ComputerSpecificationValue(var id: Int?, val specification: ComputerSpecification, val name: String) {
    companion object {
        fun newInstance(specification: ComputerSpecification, name: String) =
            ComputerSpecificationValue(null, specification, name)
    }
}
