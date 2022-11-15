package rf.vacation35.extension

import java.time.Duration

val Duration.absoluteValue
    get(): Duration {
        return if (isNegative) plusDays(1) else this
    }
