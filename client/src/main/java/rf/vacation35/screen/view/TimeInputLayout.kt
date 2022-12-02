package rf.vacation35.screen.view

import android.app.TimePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import rf.vacation35.R
import rf.vacation35.timeFormatter
import java.time.LocalTime

class TimeInputLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.textInputStyle
) : TextInputLayout(context, attrs, defStyleAttr) {

    var mTime: LocalTime? = null
        set(value) {
            field = value
            mEditText.setText(value?.let { timeFormatter.format(it) })
        }

    private val mEditText = EditText(context).apply {
        isFocusable = false
        maxLines = 1
    }

    init {
        addView(mEditText)
        endIconMode = END_ICON_CUSTOM
        setEndIconDrawable(R.drawable.ic_baseline_access_time_24)
        setEndIconOnClickListener {
            val initTime = mTime ?: LocalTime.now()
            TimePickerDialog(context, { _, hourOfDay, minute ->
                mTime = LocalTime.of(hourOfDay, minute)
            }, initTime.hour, initTime.minute, true).show()
        }
    }

    fun setTime(value: Int?) {
        mTime = value?.let { LocalTime.ofSecondOfDay(it.toLong()) }
    }
}
