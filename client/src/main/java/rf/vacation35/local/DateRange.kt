package rf.vacation35.local

import rf.vacation35.extension.inc
import rf.vacation35.extension.minus
import rf.vacation35.extension.plus
import java.time.LocalDate
import kotlin.math.max

infix fun ClosedRange<LocalDate>.cross(other: DateRange): Boolean {
    return other.endInclusive >= start && other.start <= endInclusive
}

class DateRange(
    override val start: LocalDate,
    override val endInclusive: LocalDate
) : ClosedRange<LocalDate>, Iterable<LocalDate> {

    constructor(start: LocalDate, count: Int): this(start, start + max(0, count - 1))

    constructor(count: Int, end: LocalDate): this(end - max(0, count - 1), end)

    constructor(parent: DateRange, start: Int, end: Int): this(
        parent.start + max(0, start - 1),
        parent.start + max(0, end - 1)
    )

    constructor(range: List<LocalDate>): this(range[0], range[1])

    operator fun get(i: Long): LocalDate {
        return start + i
    }

    override fun iterator() = DateIterator(start, endInclusive)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DateRange
        if (start != other.start) return false
        if (endInclusive != other.endInclusive) return false
        return true
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + endInclusive.hashCode()
        return result
    }

    override fun toString(): String {
        return "DR(start=$start, end=$endInclusive)"
    }
}

class DateIterator(private var start: LocalDate, private val end: LocalDate) : Iterator<LocalDate> {

    override fun hasNext() = start <= end

    override fun next() = start++
}
