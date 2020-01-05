package net.wojteksz128.jsoupTest.database

import java.io.Closeable

interface DatabaseFacade<T> : Closeable {
    fun save(obj: T)
    fun save(collection: Iterable<T>)
    fun update(obj: T)
    fun update(collection: Iterable<T>)
    fun delete(obj: T)
    fun delete(collection: Iterable<T>)
}