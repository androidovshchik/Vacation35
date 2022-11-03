package rf.vacation35.remote.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import rf.vacation35.remote.dsl.UserTable

class UserDao(id: EntityID<Int>) : IntEntity(id) {

    var name by UserTable.name

    var login by UserTable.login

    var password by UserTable.password

    var isAdmin by UserTable.isAdmin

    companion object : IntEntityClass<UserDao>(UserTable)
}
