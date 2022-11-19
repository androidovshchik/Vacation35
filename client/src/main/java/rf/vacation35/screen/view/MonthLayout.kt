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

    override val mBookings = mutableListOf<Booking>()

    private val binding = MergeMonthBinding.inflate(context.layoutInflater, this)

    private val months = resources.getStringArray(R.array.month)

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    @SuppressLint("SetTextI18n")
    override fun notify(value: YearMonth?) {
        super.notify(value)
        val month = mValue
        val startDay = month.atDay(1).with(DayOfWeek.MONDAY)
        binding.tvMonth.text = "${months[month.monthValue - 1]} ${month.year}"
        binding.dv1.notify(startDay.takeIf { month.includes(it) })
        binding.dv2.notify((startDay + 1).takeIf { month.includes(it) })
        binding.dv3.notify((startDay + 2).takeIf { month.includes(it) })
        binding.dv4.notify((startDay + 3).takeIf { month.includes(it) })
        binding.dv5.notify((startDay + 4).takeIf { month.includes(it) })
        binding.dv6.notify((startDay + 5).takeIf { month.includes(it) })
        binding.dv7.notify((startDay + 6).takeIf { month.includes(it) })
        binding.dv8.notify((startDay + 7).takeIf { month.includes(it) })
        binding.dv9.notify((startDay + 8).takeIf { month.includes(it) })
        binding.dv10.notify((startDay + 9).takeIf { month.includes(it) })
        binding.dv11.notify((startDay + 10).takeIf { month.includes(it) })
        binding.dv12.notify((startDay + 11).takeIf { month.includes(it) })
        binding.dv13.notify((startDay + 12).takeIf { month.includes(it) })
        binding.dv14.notify((startDay + 13).takeIf { month.includes(it) })
        binding.dv15.notify((startDay + 14).takeIf { month.includes(it) })
        binding.dv16.notify((startDay + 15).takeIf { month.includes(it) })
        binding.dv17.notify((startDay + 16).takeIf { month.includes(it) })
        binding.dv18.notify((startDay + 17).takeIf { month.includes(it) })
        binding.dv19.notify((startDay + 18).takeIf { month.includes(it) })
        binding.dv20.notify((startDay + 19).takeIf { month.includes(it) })
        binding.dv21.notify((startDay + 20).takeIf { month.includes(it) })
        binding.dv22.notify((startDay + 21).takeIf { month.includes(it) })
        binding.dv23.notify((startDay + 22).takeIf { month.includes(it) })
        binding.dv24.notify((startDay + 23).takeIf { month.includes(it) })
        binding.dv25.notify((startDay + 24).takeIf { month.includes(it) })
        binding.dv26.notify((startDay + 25).takeIf { month.includes(it) })
        binding.dv27.notify((startDay + 26).takeIf { month.includes(it) })
        binding.dv28.notify((startDay + 27).takeIf { month.includes(it) })
        binding.dv29.notify((startDay + 28).takeIf { month.includes(it) })
        binding.dv30.notify((startDay + 29).takeIf { month.includes(it) })
        binding.dv31.notify((startDay + 30).takeIf { month.includes(it) })
        binding.dv32.notify((startDay + 31).takeIf { month.includes(it) })
        binding.dv33.notify((startDay + 32).takeIf { month.includes(it) })
        binding.dv34.notify((startDay + 33).takeIf { month.includes(it) })
        binding.dv35.notify((startDay + 34).takeIf { month.includes(it) })
        binding.dv36.notify((startDay + 35).takeIf { month.includes(it) })
        binding.dv37.notify((startDay + 36).takeIf { month.includes(it) })
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        children.forEach {
            if (it is TemporalView<*>) {
                it.setOnClickListener(listener)
            }
        }
    }
}
