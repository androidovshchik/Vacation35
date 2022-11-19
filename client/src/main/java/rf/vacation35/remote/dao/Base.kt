package rf.vacation35.remote.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
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
        val id: Int,
        val name: String,
    ) {

        constructor(row: ResultRow): this(
            row[Bases.id].value,
            row[Bases.name],
        )
    }

    companion object : IntEntityClass<Base>(Bases)
}
