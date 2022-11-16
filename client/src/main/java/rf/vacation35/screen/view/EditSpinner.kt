package rf.vacation35.screen.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
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

    private val adapter = ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, mutableListOf(""))

    init {
        threshold = 1
        inputType = InputType.TYPE_NULL
        setAdapter(adapter)
        setOnTouchListener { _, _ ->
            showDropDown()
            false
        }
    }

    fun updateList(items: List<String>) {
        adapter.clear()
        adapter.addAll("")
        if (items.isNotEmpty()) {
            adapter.addAll("Все")
        }
        adapter.addAll(items)
        adapter.notifyDataSetChanged()
    }
}
