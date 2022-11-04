package rf.vacation35.remote.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import rf.vacation35.local.UserData
import rf.vacation35.remote.dsl.UserTable

class UserDao(id: EntityID<Int>) : IntEntity(id) {

    var name by UserTable.name

    var login by UserTable.login

    var password by UserTable.password

    var accessPrice by UserTable.accessPrice

    var accessBooking by UserTable.accessBooking

    var admin by UserTable.admin

    fun getData(): UserData {
        return UserData(
            id.value,
            name,
            login,
            password,
            accessPrice,
            accessBooking,
            admin
        )
    }

    companion object : IntEntityClass<UserDao>(UserTable)
}
