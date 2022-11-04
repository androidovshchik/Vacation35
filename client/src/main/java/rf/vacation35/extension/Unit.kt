package rf.vacation35.extension

import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import rf.vacation35.BuildConfig

inline fun <T> transact(crossinline statement: Transaction.() -> T): T {
    return transaction {
        if (BuildConfig.DEBUG) {
            addLogger(StdOutSqlLogger)
        }
        statement()
    }
}
