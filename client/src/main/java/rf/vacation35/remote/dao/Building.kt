package rf.vacation35.remote.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import rf.vacation35.remote.dsl.Buildings
import java.time.LocalTime

class Building(id: EntityID<Int>) : IntEntity(id) {

    var base by Base referencedOn Buildings.base

    var name by Buildings.name

    var color by Buildings.color

    var entryTime by Buildings.entryTime

    var exitTime by Buildings.exitTime

    fun toRaw(): Raw {
        return Raw(
            id.value,
            base.toRaw(),
            name,
            color,
            entryTime,
            exitTime,
        )
    }

    class Raw(
        var id: Int,
        var base: Base.Raw,
        var name: String,
        var color: String,
        var entryTime: LocalTime?,
        var exitTime: LocalTime?,
    ) {

        constructor(row: ResultRow): this(
            row[Buildings.id].value,
            Base.Raw(row),
            row[Buildings.name],
            row[Buildings.color],
            row[Buildings.entryTime],
            row[Buildings.exitTime],
        )
    }

    companion object : IntEntityClass<Building>(Buildings)
}
