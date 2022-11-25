package rf.vacation35.remote.dao

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import rf.vacation35.remote.dsl.Buildings
import java.time.LocalTime

class Building(id: EntityID<Int>) : IntEntity(id), Rawable<Building.Raw> {

    var base by Base referencedOn Buildings.base

    var name by Buildings.name

    var color by Buildings.color

    var entryTime by Buildings.entryTime

    var exitTime by Buildings.exitTime

    override fun toRaw(): Raw {
        return Raw(
            id.value,
            name,
            color,
            entryTime,
            exitTime,
        ).also {
            it.base = base.toRaw()
        }
    }

    @Parcelize
    class Raw(
        val id: Int,
        val name: String,
        val color: String,
        val entryTime: LocalTime?,
        val exitTime: LocalTime?,
        var base: Base.Raw? = null
    ) : Parcelable {

        constructor(row: ResultRow): this(
            row[Buildings.id].value,
            row[Buildings.name],
            row[Buildings.color],
            row[Buildings.entryTime],
            row[Buildings.exitTime],
        ) {
            try {
                base = Base.Raw(row)
            } catch (ignored: Throwable) {
            }
        }
    }

    companion object : IntEntityClass<Building>(Buildings)
}
