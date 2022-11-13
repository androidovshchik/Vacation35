package rf.vacation35.remote.dsl

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object BuildingTable : IntIdTable("buildings", "bu_id") {

    var base = integer("bu_base")

    var name = varchar("bu_name", 60)

    var color = varchar("bu_color", 20)

    var entryTime = datetime("bu_entry_time")

    var exitTime = datetime("bu_exit_time")
}
