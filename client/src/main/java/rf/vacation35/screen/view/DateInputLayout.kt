package rf.vacation35.screen.view

import android.app.DatePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import rf.vacation35.R
import rf.vacation35.dateFormatter
import java.time.Duration
import java.time.LocalDate

class DateInputLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.textInputStyle
) : TextInputLayout(context, attrs, defStyleAttr) {

    var mDate: LocalDate? = null
        set(value) {
            field = value
            mEditText.setText(value?.let { dateFormatter.format(it) })
        }

    private val mEditText = EditText(context).apply {
        isFocusable = false
        maxLines = 1
        textSize = 15.5f
    }

    init {
        addView(mEditText)
        endIconMode = END_ICON_CUSTOM
        setEndIconDrawable(R.drawable.ic_baseline_today_24)
        setEndIconOnClickListener {
            val initDate = mDate ?: LocalDate.now()
            DatePickerDialog(context, { _, year, month, dayOfMonth ->
                mDate = LocalDate.of(year, month, dayOfMonth)
            }, initDate.year, initDate.monthValue, initDate.dayOfMonth).show()
        }
    }

    fun setDate(value: Long?) {
        mDate = value?.let { LocalDate.ofEpochDay(Duration.ofSeconds(it).toDays()) }
    }
}
