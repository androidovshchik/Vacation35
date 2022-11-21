package rf.vacation35.screen.view

import android.view.View
import androidx.annotation.CallSuper
import rf.vacation35.remote.dao.Booking

interface TemporalView<T> {

    var mValue: T

    val mBookings: MutableList<Booking.Raw>

    @CallSuper
    fun update(value: T?, notify: Boolean = false) {
        if (value != null) {
            mValue = value
        }
        setVisibility(if (value != null) View.VISIBLE else View.GONE)
    }

    @CallSuper
    fun update(bookings: List<Booking.Raw>, notify: Boolean = false) {
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
