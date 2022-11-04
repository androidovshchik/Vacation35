package rf.vacation35.remote

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import rf.vacation35.extension.safeTransaction
import rf.vacation35.remote.dao.UserDao
import rf.vacation35.remote.dsl.UserTable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbApi @Inject constructor() {

    init {
        Database.connect(
            "jdbc:mysql://31.31.198.241:3306/u1617530_vacation35api",
            "com.mysql.jdbc.Driver",
            "u1617530_vacation35",
            "vRf8F661mq0\$"
        )
    }

    fun findUser(login: String, password: String) = safeTransaction({
        UserDao.find { UserTable.login eq login and (UserTable.password eq password) }
            .firstOrNull()
    }, null)
}
