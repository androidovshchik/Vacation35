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
            dv1.update(startDay.takeIf { month.includes(it) }, notify)
            dv2.update((startDay + 1).takeIf { month.includes(it) }, notify)
            dv3.update((startDay + 2).takeIf { month.includes(it) }, notify)
            dv4.update((startDay + 3).takeIf { month.includes(it) }, notify)
            dv5.update((startDay + 4).takeIf { month.includes(it) }, notify)
            dv6.update((startDay + 5).takeIf { month.includes(it) }, notify)
            dv7.update((startDay + 6).takeIf { month.includes(it) }, notify)
            dv8.update((startDay + 7).takeIf { month.includes(it) }, notify)
            dv9.update((startDay + 8).takeIf { month.includes(it) }, notify)
            dv10.update((startDay + 9).takeIf { month.includes(it) }, notify)
            dv11.update((startDay + 10).takeIf { month.includes(it) }, notify)
            dv12.update((startDay + 11).takeIf { month.includes(it) }, notify)
            dv13.update((startDay + 12).takeIf { month.includes(it) }, notify)
            dv14.update((startDay + 13).takeIf { month.includes(it) }, notify)
            dv15.update((startDay + 14).takeIf { month.includes(it) }, notify)
            dv16.update((startDay + 15).takeIf { month.includes(it) }, notify)
            dv17.update((startDay + 16).takeIf { month.includes(it) }, notify)
            dv18.update((startDay + 17).takeIf { month.includes(it) }, notify)
            dv19.update((startDay + 18).takeIf { month.includes(it) }, notify)
            dv20.update((startDay + 19).takeIf { month.includes(it) }, notify)
            dv21.update((startDay + 20).takeIf { month.includes(it) }, notify)
            dv22.update((startDay + 21).takeIf { month.includes(it) }, notify)
            dv23.update((startDay + 22).takeIf { month.includes(it) }, notify)
            dv24.update((startDay + 23).takeIf { month.includes(it) }, notify)
            dv25.update((startDay + 24).takeIf { month.includes(it) }, notify)
            dv26.update((startDay + 25).takeIf { month.includes(it) }, notify)
            dv27.update((startDay + 26).takeIf { month.includes(it) }, notify)
            dv28.update((startDay + 27).takeIf { month.includes(it) }, notify)
            dv29.update((startDay + 28).takeIf { month.includes(it) }, notify)
            dv30.update((startDay + 29).takeIf { month.includes(it) }, notify)
            dv31.update((startDay + 30).takeIf { month.includes(it) }, notify)
            dv32.update((startDay + 31).takeIf { month.includes(it) }, notify)
            dv33.update((startDay + 32).takeIf { month.includes(it) }, notify)
            dv34.update((startDay + 33).takeIf { month.includes(it) }, notify)
            dv35.update((startDay + 34).takeIf { month.includes(it) }, notify)
            dv36.update((startDay + 35).takeIf { month.includes(it) }, notify)
            dv37.update((startDay + 36).takeIf { month.includes(it) }, notify)
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
