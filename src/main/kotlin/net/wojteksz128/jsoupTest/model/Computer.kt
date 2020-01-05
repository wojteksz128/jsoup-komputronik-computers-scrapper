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
    }
}