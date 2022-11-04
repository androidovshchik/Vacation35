package rf.vacation35.remote.dsl

import org.jetbrains.exposed.dao.id.IntIdTable

object BuildingTable : IntIdTable("buildings", "bu_id") {

    var base = integer("bu_base")

    var name = varchar("bu_name", 60)

    var color = varchar("bu_color", 20)
}
