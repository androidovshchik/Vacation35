package rf.vacation35.remote

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import rf.vacation35.extension.transact
import rf.vacation35.remote.dao.BookingDao
import rf.vacation35.remote.dao.BuildingDao
import rf.vacation35.remote.dao.UserDao
import rf.vacation35.remote.dsl.BookingTable
import rf.vacation35.remote.dsl.BuildingTable
import rf.vacation35.remote.dsl.UserTable
import java.time.LocalDateTime

class DbApi private constructor() {

    init {
        Database.connect(
            "jdbc:mysql://31.31.198.241:3306/u1617530_vacation35api?characterEncoding=UTF-8&useUnicode=true",
            "com.mysql.jdbc.Driver",
            "u1617530_vacation35",
            "vRf8F661mq0\$"
        )
    }

    fun <T : Entity<*>> list(dao: EntityClass<*, T>) = transact {
        dao.all().toList()
    }

    fun <ID : Comparable<ID>, T : Entity<ID>> find(dao: EntityClass<ID, T>, id: ID) = transact {
        dao.findById(id)
    }

    fun <T : Entity<*>> create(dao: EntityClass<*, T>, init: (T) -> Unit) = transact {
        dao.new(init)
    }

    fun <T : Entity<*>> update(dao: T, action: (T) -> Unit) = transact {
        action(dao)
    }

    fun delete(dao: Entity<*>) = transact {
        dao.delete()
    }

    fun findUser(login: String, password: String) = transact {
        UserDao.find { UserTable.login eq login and (UserTable.password eq password) }
            .firstOrNull()
    }

    fun listBuildings(baseId: Int) = transact {
        BuildingDao.find { BuildingTable.base eq baseId }.toList()
    }

    fun queryBookings(start: LocalDateTime, end: LocalDateTime) = transact {
        BookingDao.find { BookingTable.entryTime less end and (BookingTable.exitTime greater start) }
            .sortedBy { it.entryTime }
    }

    companion object {

        @Volatile
        private var sInstance: DbApi? = null

        fun getInstance() = sInstance ?: synchronized(this) {
            sInstance ?: DbApi().also {
                sInstance = it
            }
        }
    }
}
