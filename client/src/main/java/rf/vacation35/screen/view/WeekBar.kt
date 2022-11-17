package rf.vacation35.screen.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import rf.vacation35.databinding.MergeWeekBarBinding
import rf.vacation35.extension.shortName
import rf.vacation35.remote.dao.BookingDao
import splitties.systemservices.layoutInflater
import java.time.DayOfWeek

class WeekBar : ConstraintLayout, TemporalView<DayOfWeek> {

    override lateinit var mValue: DayOfWeek

    override val mBookings: MutableList<BookingDao>
        get() = throw NotImplementedError()

    private val binding = MergeWeekBarBinding.inflate(context.layoutInflater, this)

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        if (isInEditMode) {
            notify(DayOfWeek.MONDAY)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun notify(value: DayOfWeek?) {
        super.notify(value)
        val weekday = mValue
        binding.tvDay1.text = weekday.shortName
        binding.tvDay2.text = (weekday + 1).shortName
        binding.tvDay3.text = (weekday + 2).shortName
        binding.tvDay4.text = (weekday + 3).shortName
        binding.tvDay5.text = (weekday + 4).shortName
        binding.tvDay6.text = (weekday + 5).shortName
        binding.tvDay7.text = (weekday + 6).shortName
    }
}
