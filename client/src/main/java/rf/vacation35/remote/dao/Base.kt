package rf.vacation35.remote.dao

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import rf.vacation35.remote.dsl.Bases

class Base(id: EntityID<Int>) : IntEntity(id), Rawable<Base.Raw> {

    var name by Bases.name

    override fun toRaw(): Raw {
        return Raw(
            id.value,
            name,
        )
    }

    @Parcelize
    class Raw(
        val id: Int,
        val name: String,
    ) : Parcelable {

        constructor(row: ResultRow): this(
            row[Bases.id].value,
            row[Bases.name],
        )
    }

    companion object : IntEntityClass<Base>(Bases)
}
