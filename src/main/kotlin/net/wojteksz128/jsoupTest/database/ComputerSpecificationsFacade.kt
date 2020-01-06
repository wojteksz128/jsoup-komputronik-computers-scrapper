package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.model.ComputerSpecification
import net.wojteksz128.jsoupTest.model.ComputerSpecificationAssignation
import net.wojteksz128.jsoupTest.model.ComputerSpecificationValue

interface ComputerSpecificationsFacade : DatabaseFacade<ComputerSpecification> {
    fun getAllWithPotentialValues(): Iterable<ComputerSpecification>
}

interface ComputerSpecificationValuesFacade : DatabaseFacade<ComputerSpecificationValue> {
    fun getAllBySpecification(specification: ComputerSpecification): Iterable<ComputerSpecificationValue>
}

interface ComputerSpecificationAssignationFacade : DatabaseFacade<ComputerSpecificationAssignation>