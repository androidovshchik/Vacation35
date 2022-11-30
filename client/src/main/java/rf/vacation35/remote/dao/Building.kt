package rf.vacation35.remote.dao

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import rf.vacation35.remote.dsl.Buildings
import java.time.LocalTime

class Building(id: EntityID<Int>) : IntEntity(id), Rawable<Building.Raw>, Nameable {

    var base by Base referencedOn Buildings.base

    override var name by Buildings.name

    var color by Buildings.color

    var entryTime by Buildings.entryTime

    var exitTime by Buildings.exitTime

    override fun toRaw(): Raw {
        return Raw(
            id.value,
            name,
            color,
            entryTime?.let { LocalTime.ofSecondOfDay(it.toLong()) },
            exitTime?.let { LocalTime.ofSecondOfDay(it.toLong()) },
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
        var base: Base.Raw? = null
    ) : RandomComparable(), Parcelable, Nameable {

        constructor(row: ResultRow): this(
            row[Buildings.id].value,
            row[Buildings.name],
            row[Buildings.color],
            row[Buildings.entryTime]?.let { LocalTime.ofSecondOfDay(it.toLong()) },
            row[Buildings.exitTime]?.let { LocalTime.ofSecondOfDay(it.toLong()) },
        ) {
            try {
                base = Base.Raw(row)
            } catch (ignored: Throwable) {
            }
        }
    }

    companion object : IntEntityClass<Building>(Buildings)
}
