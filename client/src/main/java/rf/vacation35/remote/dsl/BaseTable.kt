package rf.vacation35.remote.dsl

import org.jetbrains.exposed.dao.id.IntIdTable

object BaseTable : IntIdTable("bases", "ba_id") {

    var name = varchar("ba_name", 60)
}
