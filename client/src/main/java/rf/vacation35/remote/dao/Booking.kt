package rf.vacation35.remote.dao

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import rf.vacation35.remote.dsl.Bookings
import java.time.LocalDateTime
import java.time.ZoneOffset

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
            LocalDateTime.ofEpochSecond(entryTime, 0, ZoneOffset.UTC),
            LocalDateTime.ofEpochSecond(exitTime, 0, ZoneOffset.UTC),
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
            LocalDateTime.ofEpochSecond(row[Bookings.entryTime], 0, ZoneOffset.UTC),
            LocalDateTime.ofEpochSecond(row[Bookings.exitTime], 0, ZoneOffset.UTC),
            row[Bookings.clientName],
            row[Bookings.phone],
            row[Bookings.bid],
        ) {
            try {
                building = Building.Raw(row)
            } catch (ignored: Throwable) {
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Raw
            if (id != other.id) return false
            return true
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }
    }

    companion object : LongEntityClass<Booking>(Bookings)
}
