package rf.vacation35.remote.dsl

import org.jetbrains.exposed.dao.id.LongIdTable

object PriceTable : LongIdTable("prices", "p_id") {

    var building = integer("p_building")

    var startMillis = integer("p_start_millis")

    var endMillis = integer("p_end_millis")

    var rub = integer("p_rub")

    var discountPer = integer("p_discount_per").default(0)
}
