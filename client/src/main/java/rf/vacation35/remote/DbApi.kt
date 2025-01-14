package rf.vacation35.remote

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import rf.vacation35.extension.plus
import rf.vacation35.extension.transact
import rf.vacation35.remote.dao.Booking
import rf.vacation35.remote.dao.Building
import rf.vacation35.remote.dao.User
import rf.vacation35.remote.dsl.Bases
import rf.vacation35.remote.dsl.Bookings
import rf.vacation35.remote.dsl.Buildings
import rf.vacation35.remote.dsl.Users
import java.time.LocalDate
import java.time.ZoneOffset

class DbApi private constructor() {

    init {
        Database.connect(
            "jdbc:mysql://31.31.198.241:3306/u1617530_vacation35api?characterEncoding=UTF-8&useUnicode=true",
            "com.mysql.jdbc.Driver",
            "u1617530_vacation35",
            "vRf8F661mq0\$",
            databaseConfig = DatabaseConfig {
                defaultRepetitionAttempts = 0
            }
        )
    }

    fun <T : Entity<*>> list(dao: EntityClass<*, T>) = transact {
        dao.all().toList()
    }

    fun <ID : Comparable<ID>, T : Entity<ID>> find(dao: EntityClass<ID, T>, id: ID, custom: (T?) -> Unit = {}) = transact {
        dao.findById(id).apply { custom(this) }
    }

    fun <T : Entity<*>> insert(dao: EntityClass<*, T>, init: (T) -> Unit) = transact {
        dao.new(init)
    }

    fun <T : Entity<*>> update(dao: T, action: (T) -> Unit) = transact {
        action(dao)
    }

    fun delete(dao: Entity<*>) = transact {
        dao.delete()
    }

    fun listBuildings(baseId: Int? = null) = transact {
        Buildings.innerJoin(Bases, { base }, { Bases.id })
            .run {
                if (baseId != null && baseId > 0) {
                    select { Buildings.base eq baseId }
                } else {
                    selectAll()
                }
            }
            .map { Building.Raw(it) }
            .sortedBy { it.name }
    }

    fun listBuildings(buildingIds: List<Int>) = transact {
        Buildings.innerJoin(Bases, { base }, { Bases.id })
            .select { Buildings.id inList buildingIds }
            .map { Building.Raw(it) }
            .sortedBy { it.name }
    }

    fun listBookings(buildingIds: List<Int>, start: LocalDate, end: LocalDate, bids: Boolean? = null) = transact {
        val startDay = start.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        val endNextDay = (end + 1).atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        var where: Op<Boolean> = Buildings.id inList buildingIds
        if (bids != null) {
            where = where and (Bookings.bid eq bids)
        }
        where = where and (Bookings.entryTime less endNextDay and (Bookings.exitTime greaterEq startDay))
        Bookings.innerJoin(Buildings, { building }, { Buildings.id })
            .select { where }
            .map { Booking.Raw(it) }
            .sortedByDescending { it.endInclusive }
    }

    fun findUser(login: String, password: String) = transact {
        User.find { Users.login eq login and (Users.password eq password) }
            .firstOrNull()
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
