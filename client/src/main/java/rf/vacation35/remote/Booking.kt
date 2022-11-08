package rf.vacation35.remote

import android.provider.CalendarContract.Events
import android.provider.CalendarContract.Attendees
import com.android.calendar.Event
import rf.vacation35.remote.dao.BookingDao

class Booking : Event {

    constructor(dao: BookingDao): super() {
        id = 0
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
        status = Events.STATUS_CONFIRMED
        selfAttendeeStatus = Attendees.ATTENDEE_STATUS_NONE
    }
}
