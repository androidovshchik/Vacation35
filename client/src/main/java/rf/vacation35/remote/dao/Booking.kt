package rf.vacation35.remote.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import rf.vacation35.remote.dsl.Bookings
import java.time.LocalDateTime

class Booking(id: EntityID<Long>) : LongEntity(id) {

    var building by Building optionalReferencedOn Bookings.building

    var entryTime by Bookings.entryTime

    var exitTime by Bookings.exitTime

    var clientName by Bookings.clientName

    var phone by Bookings.phone

    var bid by Bookings.bid

    fun toRaw(): Raw {
        return Raw(
            id.value,
            building?.toRaw(),
            entryTime,
            exitTime,
            clientName,
            phone,
            bid,
        )
    }

    class Raw(
        var id: Long,
        var building: Building.Raw?,
        var entryTime: LocalDateTime,
        var exitTime: LocalDateTime,
        var clientName: String,
        var phone: String,
        var bid: Boolean,
    ) {

        constructor(row: ResultRow): this(
            row[Bookings.id].value,
            try { Building.Raw(row) } catch (e: Throwable) { null },
            row[Bookings.entryTime],
            row[Bookings.exitTime],
            row[Bookings.clientName],
            row[Bookings.phone],
            row[Bookings.bid],
        )
    }

    companion object : LongEntityClass<Booking>(Bookings)
}
