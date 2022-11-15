package rf.vacation35.extension

import java.time.LocalDate
import java.time.YearMonth

operator fun YearMonth.inc(): YearMonth = plusMonths(1)

operator fun YearMonth.dec(): YearMonth = minusMonths(1)

operator fun YearMonth.plus(days: Long): YearMonth = plusMonths(days)

operator fun YearMonth.plus(days: Int): YearMonth = plusMonths(days.toLong())

operator fun YearMonth.minus(days: Long): YearMonth = minusMonths(days)

operator fun YearMonth.minus(days: Int): YearMonth = minusMonths(days.toLong())

fun YearMonth.includes(date: LocalDate?): Boolean {
    return date != null && YearMonth.from(date) == this
}
