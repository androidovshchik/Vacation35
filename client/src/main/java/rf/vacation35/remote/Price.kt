package rf.vacation35.remote

import android.provider.CalendarContract
import com.android.calendar.Event
import rf.vacation35.remote.dao.PriceDao

class Price(dao: PriceDao) : Event() {

    init {
        id = dao.id.value
        title = null
        color = 0
        location = null
        allDay = false
        startDay = 0
        endDay = 0
        startTime = 0
        endTime = 0
        startMillis = 0
        endMillis = 0
        hasAlarm = false
        isRepeating = false
        status = CalendarContract.Events.STATUS_TENTATIVE
        selfAttendeeStatus = CalendarContract.Attendees.ATTENDEE_STATUS_NONE
    }
}
