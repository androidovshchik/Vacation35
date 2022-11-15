package rf.vacation35.extension

import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal

infix fun Temporal.untilMillis(to: Temporal): Long {
    return ChronoUnit.MILLIS.between(this, to)
}

infix fun Temporal.untilDays(to: Temporal): Int {
    return ChronoUnit.DAYS.between(this, to).toInt()
}

infix fun Temporal.untilWeeks(to: Temporal): Int {
    return ChronoUnit.WEEKS.between(this, to).toInt()
}
