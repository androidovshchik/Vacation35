package rf.vacation35

import java.util.concurrent.TimeUnit

const val EXTRA_USER_ID = "user_id"
const val EXTRA_BASE_ID = "base_id"
const val EXTRA_BUILDING_ID = "building_id"
const val EXTRA_BOOKING_ID = "booking_id"

const val EXTRA_BIDS = "bids"
const val EXTRA_DATE = "date"

val onStartDelay = TimeUnit.SECONDS.toMillis(60)

val checkUserDelay = TimeUnit.SECONDS.toMillis(60)

val bbRepeatDelay = TimeUnit.SECONDS.toMillis(60)
val bbErrorDelay = TimeUnit.SECONDS.toMillis(10)
