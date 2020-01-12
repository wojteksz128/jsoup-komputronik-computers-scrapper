package net.wojteksz128.jsoupTest.database

import org.jetbrains.exposed.sql.transactions.transaction
import java.io.Closeable

interface DatabaseFacade<T> : Closeable {
    var appDatabase: AppDatabase

    fun getById(id: Int): T
    fun getAllByIds(ids: Iterable<Int>): Iterable<T>
    fun save(obj: T)

    fun save(collection: Iterable<T>) = transaction {
        collection.forEach { save(it) }
    }

    fun update(obj: T)
    fun update(collection: Iterable<T>) = transaction {
        collection.forEach { update(it) }
    }

    fun delete(obj: T)
    fun delete(collection: Iterable<T>) = transaction {
        collection.forEach { delete(it) }
    }

    override fun close() {
    }
}