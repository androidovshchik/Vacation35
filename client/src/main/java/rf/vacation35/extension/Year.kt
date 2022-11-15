package rf.vacation35.extension

import java.time.Year

operator fun Year.inc(): Year = plusYears(1)

operator fun Year.dec(): Year = minusYears(1)

operator fun Year.plus(days: Long): Year = plusYears(days)

operator fun Year.plus(days: Int): Year = plusYears(days.toLong())

operator fun Year.minus(days: Long): Year = minusYears(days)

operator fun Year.minus(days: Int): Year = minusYears(days.toLong())
