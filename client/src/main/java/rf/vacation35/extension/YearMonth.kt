package rf.vacation35.extension

import java.time.LocalDate
import java.time.YearMonth

operator fun YearMonth.inc(): YearMonth = plusMonths(1)

operator fun YearMonth.dec(): YearMonth = minusMonths(1)

operator fun YearMonth.plus(months: Long): YearMonth = plusMonths(months)

operator fun YearMonth.plus(months: Int): YearMonth = plusMonths(months.toLong())

operator fun YearMonth.minus(months: Long): YearMonth = minusMonths(months)

operator fun YearMonth.minus(months: Int): YearMonth = minusMonths(months.toLong())

fun YearMonth.includes(date: LocalDate?): Boolean {
    return date != null && YearMonth.from(date) == this
}
