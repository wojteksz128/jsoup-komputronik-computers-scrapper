package dao

import model.Computer
import model.PcSpecDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.Closeable

interface DAOFacadePc:Closeable{
    fun init()
    fun createComputer(name:String, url:String, price:Int, params: Map<String, MutableList<String>>)
    fun createPcParametersList(params: Map<String, MutableList<String>>, pcId:Int)
}

class DAOFacadeDatabase(val db: Database): DAOFacadePc{

    override fun init() = transaction(db) {
        SchemaUtils.create(Computers)
        SchemaUtils.create(Specifications)
    }

    override fun createComputer(name:String, url:String, price:Int, params: Map<String, MutableList<String>>) = transaction(db) {
        val pcId = Computers.insert {
            it[Computers.name] = name; it[Computers.url] = url; it[Computers.price] = price  } get Computers.id
        Unit
        createPcParametersList(params, pcId)
    }

    override fun createPcParametersList(params: Map<String, MutableList<String>>, pcId:Int) = transaction(db) {

        for( attribute in params.keys){
            for(attributeValue in params.get(attribute)!!){
                Specifications.insert { it[Specifications.attributeName] = attribute; it[Specifications.attributeValue] = attributeValue; it[Specifications.pcId] = pcId }
                Unit
            }
        }
    }

    fun getAllComputers() = transaction(db){
        Computers.selectAll().map {
            Computer(it[Computers.id], it[Computers.name], it[Computers.url], it[Computers.price] )
        }
    }

    fun getPcSpec(pcId: Int) = transaction(db){
        Specifications.select {Specifications.pcId eq pcId}.map {
            PcSpecDto( it[Specifications.attributeName], it[Specifications.attributeValue], it[Specifications.pcId])
        }
    }

    override fun close() = transaction(db) {
    }
}