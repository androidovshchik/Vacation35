package rf.vacation35.screen.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import rf.vacation35.R
import rf.vacation35.extension.*
import rf.vacation35.remote.dao.Booking
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.roundToInt

class DayView : View, TemporalView<LocalDate> {

    override lateinit var mValue: LocalDate

    override val mBookings = mutableListOf<Booking>()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(20)
        textAlign = Paint.Align.LEFT
        strokeWidth = dp(1)
    }

    private val minHeight = resources.getDimension(R.dimen.day_min_height)

    private val dayMargin get() = dp(5)

    private val stripHeight get() = dp(10)

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
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val reservedSize = ((minHeight - (paint.textSize + 2 * dayMargin)) / stripHeight).roundToInt()
        val totalHeight = (minHeight + max(0, mBookings.size - reservedSize) * stripHeight).roundToInt()
        setMeasuredDimension(getDefaultSize(suggestedMinimumWidth, widthMeasureSpec), totalHeight)
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
        val strokeWidthHalf = paint.strokeWidth / 2

        val date = mValue
        val day = date.dayOfMonth.toString()

        val hasBids = mBookings.any { it.bid }

        paint.style = Paint.Style.FILL
        canvas.drawColor(when {
            hasBids -> 0xffbdbdbd.toInt()
            date.dayOfWeek.value % 2 != 0 -> 0xffeeeeee.toInt()
            else -> 0xfffafafa.toInt()
        })

        var bounds = paint.getTextBounds(day)

        paint.style = Paint.Style.FILL
        paint.color = if (hasBids) Color.WHITE else Color.BLACK
        canvas.drawText(day, width - dayMargin - bounds.width(), dayMargin + bounds.height(), paint)

        paint.style = Paint.Style.FILL
        var y = paint.textSize + 2 * dayMargin
        mBookings.forEachIndexed { _, booking ->
            paint.color = Color.parseColor(booking.building?.color ?: "#000000")
            canvas.drawRect(0f, y, width, y + stripHeight, paint)
            y += stripHeight
        }

        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        canvas.drawLine(strokeWidthHalf, strokeWidthHalf, width - strokeWidthHalf, strokeWidthHalf, paint)
    }

    override fun getChildCount() = 0

    override fun getChildAt(index: Int): View? = null
}
