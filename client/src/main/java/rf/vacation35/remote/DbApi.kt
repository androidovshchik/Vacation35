package rf.vacation35.remote

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import rf.vacation35.extension.transact
import rf.vacation35.remote.dao.*
import rf.vacation35.remote.dsl.BookingTable
import rf.vacation35.remote.dsl.BuildingTable
import rf.vacation35.remote.dsl.UserTable
import java.time.LocalDateTime

class DbApi private constructor() {

    init {
        Database.connect(
            "jdbc:mysql://31.31.198.241:3306/u1617530_vacation35api?characterEncoding=utf8&useUnicode=true",
            "com.mysql.jdbc.Driver",
            "u1617530_vacation35",
            "vRf8F661mq0\$"
        )
    }

    fun listUsers() = transact {
        UserDao.all().toList()
    }

    fun findUser(id: Int) = transact {
        UserDao.findById(id)
    }

    fun findUser(login: String, password: String) = transact {
        UserDao.find { UserTable.login eq login and (UserTable.password eq password) }
            .firstOrNull()
    }

    fun listBases() = transact {
        BaseDao.all().toList()
    }

    fun findBase(id: Int) = transact {
        BaseDao.findById(id)
    }

    fun listBuildings(baseId: Int) = transact {
        BuildingDao.find { BuildingTable.base eq baseId }.toList()
    }

    fun findBuilding(id: Int) = transact {
        BuildingDao.findById(id)
    }

    fun listPrices() = transact {
        PriceDao.all().toList()
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
