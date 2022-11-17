package rf.vacation35.remote.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import rf.vacation35.remote.dsl.BuildingTable

class BuildingDao(id: EntityID<Int>) : IntEntity(id) {

    var base by BaseDao referencedOn BuildingTable.base

    var name by BuildingTable.name

    var color by BuildingTable.color

    var entryTime by BuildingTable.entryTime

    var exitTime by BuildingTable.exitTime

    companion object : IntEntityClass<BuildingDao>(BuildingTable)
}
