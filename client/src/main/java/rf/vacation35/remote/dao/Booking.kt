package rf.vacation35.remote.dao

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import rf.vacation35.remote.dsl.Bookings
import java.time.LocalDateTime

class Booking(id: EntityID<Long>) : LongEntity(id), Rawable<Booking.Raw> {

    var building by Building optionalReferencedOn Bookings.building

    var entryTime by Bookings.entryTime

    var exitTime by Bookings.exitTime

    var clientName by Bookings.clientName

    var phone by Bookings.phone

    var bid by Bookings.bid

    override fun toRaw(): Raw {
        return Raw(
            id.value,
            entryTime,
            exitTime,
            clientName,
            phone,
            bid,
        ).also {
            it.building = building?.toRaw()
        }
    }

    @Parcelize
    class Raw(
        val id: Long,
        override val start: LocalDateTime,
        override val endInclusive: LocalDateTime,
        val clientName: String,
        val phone: String,
        val bid: Boolean,
        var building: Building.Raw? = null
    ) : ClosedRange<LocalDateTime>, Parcelable {

        constructor(row: ResultRow): this(
            row[Bookings.id].value,
            row[Bookings.entryTime],
            row[Bookings.exitTime],
            row[Bookings.clientName],
            row[Bookings.phone],
            row[Bookings.bid],
        ) {
            try {
                building = Building.Raw(row)
            } catch (ignored: Throwable) {
            }
        }
    }

    companion object : LongEntityClass<Booking>(Bookings)
}
