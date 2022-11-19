package rf.vacation35.remote.dao

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import rf.vacation35.remote.dsl.Users

class User(id: EntityID<Int>) : IntEntity(id) {

    var name by Users.name

    var login by Users.login

    var password by Users.password

    var accessPrice by Users.accessPrice

    var accessBooking by Users.accessBooking

    var admin by Users.admin

    fun toRaw(): Raw {
        return Raw(
            id.value,
            name,
            login,
            password,
            accessPrice,
            accessBooking,
            admin
        )
    }

    @Keep
    @Serializable
    class Raw(
        val id: Int,
        val name: String,
        val login: String,
        val password: String,
        val accessPrice: Boolean = false,
        val accessBooking: Boolean = false,
        val admin: Boolean = false,
    ) {

        constructor(row: ResultRow): this(
            row[Users.id].value,
            row[Users.name],
            row[Users.login],
            row[Users.password],
            row[Users.accessPrice],
            row[Users.accessBooking],
            row[Users.admin],
        )
    }

    companion object : IntEntityClass<User>(Users)
}
