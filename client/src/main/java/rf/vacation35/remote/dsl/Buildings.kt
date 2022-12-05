package rf.vacation35.remote.dsl

import org.jetbrains.exposed.dao.id.IntIdTable
import rf.vacation35.BuildConfig

object Buildings : IntIdTable("${if (BuildConfig.DEBUG) "_" else ""}buildings", "bu_id") {

    val base = reference("bu_base", Bases)

    var name = varchar("bu_name", 60)

    var color = varchar("bu_color", 20)

    var entryTime = integer("bu_entry_time").nullable()

    var exitTime = integer("bu_exit_time").nullable()

    var priceList = text("bu_price_list").default("[]")
}
