package rf.vacation35.screen.view

import android.view.View
import androidx.annotation.CallSuper
import rf.vacation35.remote.dao.BookingDao

interface TemporalView<T> {

    var mValue: T

    val mBookings: MutableList<BookingDao>

    @CallSuper
    fun notify(value: T?) {
        if (value != null) {
            mValue = value
        }
        setVisibility(if (value != null) View.VISIBLE else View.GONE)
    }

    @CallSuper
    fun notify(value: T?, bookings: List<BookingDao>) {
        update(bookings)
        notify(value)
    }

    @CallSuper
    fun update(bookings: List<BookingDao>, notify: Boolean = false) {
        (0 until getChildCount()).forEach {
            val child = getChildAt(it)
            if (child is TemporalView<*>) {
                child.update(bookings, notify)
            }
        }
    }

    fun setVisibility(visibility: Int)

    fun getChildCount(): Int

    fun getChildAt(index: Int): View?
}
