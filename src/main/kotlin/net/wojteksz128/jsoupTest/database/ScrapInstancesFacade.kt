package net.wojteksz128.jsoupTest.database

import net.wojteksz128.jsoupTest.model.ScrapInstance

interface ScrapInstancesFacade : DatabaseFacade<ScrapInstance> {
    fun getById(id: Int): ScrapInstance
}