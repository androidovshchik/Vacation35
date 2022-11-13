package rf.vacation35.remote.dsl

import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable("users", "u_id") {

    var name = varchar("u_name", 60)

    var login = varchar("u_login", 30).uniqueIndex()

    var password = varchar("u_password", 30)

    var accessPrice = bool("u_access_price").default(false)

    var accessBooking = bool("u_access_booking").default(false)

    var admin = bool("u_admin").default(false)
}
