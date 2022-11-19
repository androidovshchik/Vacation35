package rf.vacation35.remote.dsl

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object Bookings : LongIdTable("bookings", "bo_id") {

    val building = reference("bo_building", Buildings).nullable()

    var entryTime = datetime("bo_entry_time")

    var exitTime = datetime("bo_exit_time")

    var clientName = varchar("bo_client_name", 200)

    var phone = varchar("bo_phone", 40)

    var bid = bool("bo_bid").default(true)
}