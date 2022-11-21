package rf.vacation35.extension

import java.time.LocalDateTime

infix fun ClosedRange<LocalDateTime>.cross(other: ClosedRange<LocalDateTime>): Boolean {
    return other.endInclusive >= start && other.start <= endInclusive
}
