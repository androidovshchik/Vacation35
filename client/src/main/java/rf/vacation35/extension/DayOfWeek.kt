package rf.vacation35.extension

import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

val DayOfWeek.shortName: String
    get() = getDisplayName(TextStyle.SHORT, Locale.getDefault())
