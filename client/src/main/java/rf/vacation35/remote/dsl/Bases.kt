package rf.vacation35.remote.dsl

import org.jetbrains.exposed.dao.id.IntIdTable
import rf.vacation35.BuildConfig

object Bases : IntIdTable("${if (BuildConfig.DEBUG) "_" else ""}bases", "ba_id") {

    var name = varchar("ba_name", 60)
}
