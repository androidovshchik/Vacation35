package rf.vacation35.remote

import org.jetbrains.exposed.sql.Database
import rf.vacation35.extension.safeTransaction
import rf.vacation35.remote.dao.UserDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbManager @Inject constructor() {

    init {
        Database.connect(
            "jdbc:mysql://31.31.198.241:3306/u1617530_vacation35api",
            "com.mysql.jdbc.Driver",
            "u1617530_vacation35",
            "vRf8F661mq0\$"
        )
    }

    fun getUsers() = safeTransaction({
        UserDao.all()
    }, null)
}
