package rf.vacation35.remote.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import rf.vacation35.remote.dsl.BaseTable

class BaseDao(id: EntityID<Int>) : IntEntity(id) {

    var name by BaseTable.name

    companion object : IntEntityClass<BaseDao>(BaseTable)
}
