package rf.vacation35.screen.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import rf.vacation35.extension.*
import rf.vacation35.remote.dao.Booking
import java.time.LocalDate
import java.time.YearMonth

class DayView : View, TemporalView<LocalDate> {

    override lateinit var mValue: LocalDate

    override val mBookings = mutableListOf<Booking>()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(20)
        textAlign = Paint.Align.LEFT
        strokeWidth = dp(1)
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            foreground = ContextCompat.getDrawable(context, context.getIdRes(android.R.attr.selectableItemBackground))
        }
    }

    override fun notify(value: LocalDate?) {
        super.notify(value)
        invalidate()
    }

    override fun update(bookings: List<Booking>, notify: Boolean) {

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

        val width = width.toFloat()
        val height = height.toFloat()
        val dayMargin = dp(5)
        val stripHeight = dp(10)
        val strokeWidthHalf = paint.strokeWidth / 2

        val date = mValue
        val month = YearMonth.from(date)

        val hasBids = mBookings.any { it.bid }
        val day = date.dayOfMonth.toString()
        var bounds = paint.getTextBounds(day)

        paint.style = Paint.Style.FILL
        canvas.drawColor(if (hasBids) 0xff9E9E9E.toInt() else Color.WHITE)

        paint.style = Paint.Style.FILL
        paint.color = if (hasBids) Color.WHITE else Color.BLACK
        canvas.drawText(day, width - dayMargin - bounds.width(), dayMargin + bounds.height(), paint)

        paint.style = Paint.Style.FILL
        var y = paint.textSize + 2 * dayMargin
        mBookings.forEachIndexed { i, booking ->
            paint.color = Color.parseColor(booking.building?.color ?: "#000000")
            canvas.drawRect(0f, y, width, y + stripHeight, paint)
            y += stripHeight
        }

        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        // left line
        canvas.drawLine(strokeWidthHalf, strokeWidthHalf, strokeWidthHalf, height - strokeWidthHalf, paint)
        // top line
        canvas.drawLine(strokeWidthHalf, strokeWidthHalf, width - strokeWidthHalf, strokeWidthHalf, paint)
        if (date == month.atEndOfMonth()) {
            // right line
            canvas.drawLine(width - strokeWidthHalf, strokeWidthHalf, width - strokeWidthHalf, height - strokeWidthHalf, paint)
        }
        // bottom line
        if (!month.includes(date + 7)) {
            canvas.drawLine(strokeWidthHalf, height - strokeWidthHalf, width - strokeWidthHalf, height - strokeWidthHalf, paint)
        }
    }

    override fun getChildCount() = 0

    override fun getChildAt(index: Int): View? = null
}
