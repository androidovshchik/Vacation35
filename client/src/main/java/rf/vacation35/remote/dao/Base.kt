package rf.vacation35.remote.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import rf.vacation35.remote.dsl.Bases

class Base(id: EntityID<Int>) : IntEntity(id) {

    var name by Bases.name

    fun toRaw(): Raw {
        return Raw(
            id.value,
            name,
        )
    }

    class Raw(
        var id: Int,
        var name: String,
    )

    companion object : IntEntityClass<Base>(Bases)
}
