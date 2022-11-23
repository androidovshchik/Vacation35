package rf.vacation35.screen.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import rf.vacation35.databinding.MergeWeekBinding
import rf.vacation35.extension.shortName
import rf.vacation35.remote.dao.Booking
import splitties.systemservices.layoutInflater
import java.time.DayOfWeek

class WeekBar : ConstraintLayout, TemporalView<DayOfWeek> {

    override lateinit var mValue: DayOfWeek

    override val mBookings: MutableList<Booking.Raw>
        get() = throw NotImplementedError()

    private val binding = MergeWeekBinding.inflate(context.layoutInflater, this)

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        update(DayOfWeek.MONDAY)
    }

    @SuppressLint("SetTextI18n")
    override fun update(value: DayOfWeek?, notify: Boolean) {
        super.update(value, notify)
        val weekday = mValue
        with(binding) {
            tvDay1.text = weekday.shortName
            tvDay2.text = (weekday + 1).shortName
            tvDay3.text = (weekday + 2).shortName
            tvDay4.text = (weekday + 3).shortName
            tvDay5.text = (weekday + 4).shortName
            tvDay6.text = (weekday + 5).shortName
            tvDay7.text = (weekday + 6).shortName
        }
    }
}
