package rf.vacation35.screen.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import rf.vacation35.extension.dp
import rf.vacation35.extension.getIdRes
import rf.vacation35.extension.sp
import rf.vacation35.remote.dao.BookingDao
import java.time.LocalDate

class DayView : View, TemporalView<LocalDate> {

    override lateinit var mValue: LocalDate

    override val mBookings = mutableListOf<BookingDao>()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        textSize = sp(20)

        strokeWidth = dp(1)
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            foreground = ContextCompat.getDrawable(context, context.getIdRes(android.R.attr.actionBarItemBackground))
        }
    }

    override fun notify(value: LocalDate?) {
        super.notify(value)
        invalidate()
    }

    override fun update(bookings: List<BookingDao>, notify: Boolean) {

        super.update(bookings, notify)
        if (notify) {
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (isInEditMode) {
            mValue = LocalDate.now()
        }
        if (!::mValue.isInitialized) {
            return
        }
    }

    override fun getChildCount() = 0

    override fun getChildAt(index: Int): View? = null
}
