package rf.vacation35.extension

import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import rf.vacation35.BuildConfig
import timber.log.Timber

inline fun <T> safeTransaction(crossinline statement: Transaction.() -> T, default: T): T {
    return try {
        transaction {
            if (BuildConfig.DEBUG) {
                addLogger(StdOutSqlLogger)
            }
            statement()
        }
    } catch (e: Throwable) {
        Timber.e(e)
        default
    }
}
