package rf.vacation35.remote.dsl

import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable("users", "u_id") {

    var name = varchar("u_name", 60)

    var login = varchar("u_login", 30)

    var password = varchar("u_password", 15)

    var isAdmin = bool("u_is_admin")
}
