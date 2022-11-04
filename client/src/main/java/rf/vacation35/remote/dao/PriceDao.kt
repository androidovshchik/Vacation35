package rf.vacation35.remote.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import rf.vacation35.remote.dsl.PriceTable

class PriceDao(id: EntityID<Long>) : LongEntity(id) {

    var building by PriceTable.building

    var startMillis by PriceTable.startMillis

    var endMillis by PriceTable.endMillis

    var rub by PriceTable.rub

    var discountPer by PriceTable.discountPer

    companion object : LongEntityClass<PriceDao>(PriceTable)
}

