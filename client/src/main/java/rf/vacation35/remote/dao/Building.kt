package rf.vacation35.remote.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import rf.vacation35.remote.dsl.Buildings
import java.time.LocalTime

class Building(id: EntityID<Int>) : IntEntity(id) {

    var base by Base referencedOn Buildings.base

    var name by Buildings.name

    var color by Buildings.color

    var entryTime by Buildings.entryTime

    var exitTime by Buildings.exitTime

    fun toRaw(base: Base.Raw): Raw {
        return Raw(
            id.value,
            base,
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
    )

    companion object : IntEntityClass<Building>(Buildings)
}
