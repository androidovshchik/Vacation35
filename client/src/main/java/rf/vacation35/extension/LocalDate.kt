package rf.vacation35.extension

import rf.vacation35.local.DateRange
import java.time.LocalDate

operator fun LocalDate.inc(): LocalDate = plusDays(1)

operator fun LocalDate.dec(): LocalDate = minusDays(1)

operator fun LocalDate.plus(days: Long): LocalDate = plusDays(days)

operator fun LocalDate.plus(days: Int): LocalDate = plusDays(days.toLong())

operator fun LocalDate.minus(days: Long): LocalDate = minusDays(days)

operator fun LocalDate.minus(days: Int): LocalDate = minusDays(days.toLong())

operator fun LocalDate.rangeTo(that: LocalDate): DateRange = DateRange(this, that)
