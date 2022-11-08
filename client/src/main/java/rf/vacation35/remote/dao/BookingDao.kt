package rf.vacation35.remote.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import rf.vacation35.remote.dsl.BookingTable

class BookingDao(id: EntityID<Long>) : LongEntity(id) {

    var building by BookingTable.building

    var entryTime by BookingTable.entryTime

    var exitTime by BookingTable.exitTime

    var clientName by BookingTable.clientName

    var phone by BookingTable.phone

    var alert by BookingTable.alert

    var bid by BookingTable.bid

    companion object : LongEntityClass<BookingDao>(BookingTable)
}