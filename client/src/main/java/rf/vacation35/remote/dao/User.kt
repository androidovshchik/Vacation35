package rf.vacation35.remote.dao

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import rf.vacation35.remote.dsl.Users

class User(id: EntityID<Int>) : IntEntity(id), Rawable<User.Raw>, Nameable {

    override var name by Users.name

    var login by Users.login

    var password by Users.password

    var accessPrice by Users.accessPrice

    var accessBooking by Users.accessBooking

    var admin by Users.admin

    override fun toRaw(): Raw {
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
    @Parcelize
    @Serializable
    open class Raw(
        val id: Int,
        override val name: String,
        val login: String,
        val password: String,
        val accessPrice: Boolean = false,
        val accessBooking: Boolean = false,
        val admin: Boolean = false,
    ) : Parcelable, Nameable {

        constructor(row: ResultRow): this(
            row[Users.id].value,
            row[Users.name],
            row[Users.login],
            row[Users.password],
            row[Users.accessPrice],
            row[Users.accessBooking],
            row[Users.admin],
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Raw
            if (id != other.id) return false
            if (name != other.name) return false
            if (login != other.login) return false
            if (password != other.password) return false
            if (accessPrice != other.accessPrice) return false
            if (accessBooking != other.accessBooking) return false
            if (admin != other.admin) return false
            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + name.hashCode()
            result = 31 * result + login.hashCode()
            result = 31 * result + password.hashCode()
            result = 31 * result + accessPrice.hashCode()
            result = 31 * result + accessBooking.hashCode()
            result = 31 * result + admin.hashCode()
            return result
        }
    }

    class Temp : Raw(0, "", "", "", false, false, false)

    class None : Raw(0, "", "", "", false, false, false)

    companion object : IntEntityClass<User>(Users)
}
