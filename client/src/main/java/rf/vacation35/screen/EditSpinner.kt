package rf.vacation35.screen

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import rf.vacation35.R

@SuppressLint("ClickableViewAccessibility")
class EditSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.autoCompleteTextViewStyle
) : AppCompatAutoCompleteTextView(context, attrs, defStyleAttr) {

    private val adapter = ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line)

    init {
        threshold = 1
        setAdapter(adapter)
        setOnTouchListener { _, _ ->
            showDropDown()
            false
        }
    }
}
