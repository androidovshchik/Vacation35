package rf.vacation35.remote.dao

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import rf.vacation35.dateFormatter
import rf.vacation35.remote.dsl.Buildings
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class Building(id: EntityID<Int>) : IntEntity(id), Rawable<Building.Raw>, Nameable {

    var base by Base referencedOn Buildings.base

    override var name by Buildings.name

    var color by Buildings.color

    var entryTime by Buildings.entryTime

    var exitTime by Buildings.exitTime

    var priceList by Buildings.priceList

    override fun toRaw(): Raw {
        return Raw(
            id.value,
            name,
            color,
            entryTime?.let { LocalTime.ofSecondOfDay(it.toLong()) },
            exitTime?.let { LocalTime.ofSecondOfDay(it.toLong()) },
            Json.decodeFromString(priceList)
        ).also {
            it.base = base.toRaw()
        }
    }

    @Parcelize
    class Raw(
        val id: Int,
        override val name: String,
        val color: String,
        val entryTime: LocalTime?,
        val exitTime: LocalTime?,
        val priceList: List<Price>,
        var base: Base.Raw? = null
    ) : RandomComparable(), Parcelable, Nameable {

        constructor(row: ResultRow): this(
            row[Buildings.id].value,
            row[Buildings.name],
            row[Buildings.color],
            row[Buildings.entryTime]?.let { LocalTime.ofSecondOfDay(it.toLong()) },
            row[Buildings.exitTime]?.let { LocalTime.ofSecondOfDay(it.toLong()) },
            Json.decodeFromString(row[Buildings.priceList])
        ) {
            try {
                base = Base.Raw(row)
            } catch (ignored: Throwable) {
            }
        }
    }

    @Parcelize
    @Serializable
    class Price(
        @SerialName("wd")
        val weekDay: DayOfWeek? = null,
        @SerialName("yd")
        @Serializable(with = LocalDateSerializer::class)
        val yearDay: LocalDate? = null,
        @SerialName("pr")
        val priceRub: Int = 0,
        @SerialName("dp")
        val discountPer: Int = 0,
    ) : Parcelable

    companion object : IntEntityClass<Building>(Buildings)
}

object LocalDateSerializer : KSerializer<LocalDate> {

    override val descriptor = PrimitiveSerialDescriptor(javaClass.name, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(dateFormatter.format(value))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString(), dateFormatter)
    }
}
