package rf.vacation35.screen.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import rf.vacation35.R
import rf.vacation35.databinding.MergeMonthBinding
import rf.vacation35.extension.includes
import rf.vacation35.extension.plus
import rf.vacation35.remote.dao.Booking
import splitties.systemservices.layoutInflater
import java.time.DayOfWeek
import java.time.YearMonth

class MonthLayout : ConstraintLayout, TemporalView<YearMonth> {

    override lateinit var mValue: YearMonth

    override val mBookings: MutableList<Booking.Raw>
        get() = throw NotImplementedError()

    private val binding = MergeMonthBinding.inflate(context.layoutInflater, this)

    private val months = resources.getStringArray(R.array.month)

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    @SuppressLint("SetTextI18n")
    override fun update(value: YearMonth?, notify: Boolean) {
        super.update(value, notify)
        val month = mValue
        val startDay = month.atDay(1).with(DayOfWeek.MONDAY)
        with(binding) {
            tvMonth.text = "${months[month.monthValue - 1]} ${month.year}"
            dv1.notify(startDay.takeIf { month.includes(it) })
            dv2.notify((startDay + 1).takeIf { month.includes(it) })
            dv3.notify((startDay + 2).takeIf { month.includes(it) })
            dv4.notify((startDay + 3).takeIf { month.includes(it) })
            dv5.notify((startDay + 4).takeIf { month.includes(it) })
            dv6.notify((startDay + 5).takeIf { month.includes(it) })
            dv7.notify((startDay + 6).takeIf { month.includes(it) })
            dv8.notify((startDay + 7).takeIf { month.includes(it) })
            dv9.notify((startDay + 8).takeIf { month.includes(it) })
            dv10.notify((startDay + 9).takeIf { month.includes(it) })
            dv11.notify((startDay + 10).takeIf { month.includes(it) })
            dv12.notify((startDay + 11).takeIf { month.includes(it) })
            dv13.notify((startDay + 12).takeIf { month.includes(it) })
            dv14.notify((startDay + 13).takeIf { month.includes(it) })
            dv15.notify((startDay + 14).takeIf { month.includes(it) })
            dv16.notify((startDay + 15).takeIf { month.includes(it) })
            dv17.notify((startDay + 16).takeIf { month.includes(it) })
            dv18.notify((startDay + 17).takeIf { month.includes(it) })
            dv19.notify((startDay + 18).takeIf { month.includes(it) })
            dv20.notify((startDay + 19).takeIf { month.includes(it) })
            dv21.notify((startDay + 20).takeIf { month.includes(it) })
            dv22.notify((startDay + 21).takeIf { month.includes(it) })
            dv23.notify((startDay + 22).takeIf { month.includes(it) })
            dv24.notify((startDay + 23).takeIf { month.includes(it) })
            dv25.notify((startDay + 24).takeIf { month.includes(it) })
            dv26.notify((startDay + 25).takeIf { month.includes(it) })
            dv27.notify((startDay + 26).takeIf { month.includes(it) })
            dv28.notify((startDay + 27).takeIf { month.includes(it) })
            dv29.notify((startDay + 28).takeIf { month.includes(it) })
            dv30.notify((startDay + 29).takeIf { month.includes(it) })
            dv31.notify((startDay + 30).takeIf { month.includes(it) })
            dv32.notify((startDay + 31).takeIf { month.includes(it) })
            dv33.notify((startDay + 32).takeIf { month.includes(it) })
            dv34.notify((startDay + 33).takeIf { month.includes(it) })
            dv35.notify((startDay + 34).takeIf { month.includes(it) })
            dv36.notify((startDay + 35).takeIf { month.includes(it) })
            dv37.notify((startDay + 36).takeIf { month.includes(it) })
        }
    }

    override fun update(bookings: List<Booking.Raw>, notify: Boolean) {
        super.update(bookings, notify)
        if (notify) {
            requestLayout()
        }
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        children.forEach {
            if (it is TemporalView<*>) {
                it.setOnClickListener(listener)
            }
        }
    }
}
