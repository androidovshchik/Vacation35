package rf.vacation35.extension

import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset

fun LocalDateTime.toClock(zone: ZoneOffset = ZoneOffset.UTC): Clock {
    return Clock.fixed(toInstant(zone), zone)
}
