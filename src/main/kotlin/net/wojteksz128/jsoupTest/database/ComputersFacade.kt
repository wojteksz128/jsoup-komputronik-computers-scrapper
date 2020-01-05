package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.model.Computer

interface ComputersFacade : DatabaseFacade<Computer> {
    fun getAll(): Iterable<Computer>
}
