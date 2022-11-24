package rf.vacation35.remote

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.*
import rf.vacation35.extension.transact
import rf.vacation35.remote.dao.Booking
import rf.vacation35.remote.dao.Building
import rf.vacation35.remote.dao.User
import rf.vacation35.remote.dsl.Bases
import rf.vacation35.remote.dsl.Bookings
import rf.vacation35.remote.dsl.Buildings
import rf.vacation35.remote.dsl.Users
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
        User.find { Users.login eq login and (Users.password eq password) }
            .firstOrNull()
    }

    fun listBuildings(baseId: Int? = null) = transact {
        Buildings.innerJoin(Bases, { base }, { Bases.id })
            .run {
                if (baseId != null && baseId > 0) {
                    select { Buildings.base eq baseId }
                } else {
                    selectAll()
                }
            }.map { Building.Raw(it) }
    }

    fun queryBookings(buildingIds: List<Int>, start: LocalDateTime, end: LocalDateTime) = transact {
        Bookings.innerJoin(Buildings, { building }, { Buildings.id })
            .select { Buildings.id inList buildingIds and (Bookings.entryTime lessEq end and (Bookings.exitTime greaterEq start)) }
            .map { Booking.Raw(it) }
            .sortedBy { it.start }
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
