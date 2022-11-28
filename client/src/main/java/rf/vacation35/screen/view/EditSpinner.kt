package rf.vacation35.screen.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import rf.vacation35.R
import rf.vacation35.remote.dao.Nameable
import java.util.*

@SuppressLint("ClickableViewAccessibility")
class EditSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.autoCompleteTextViewStyle
) : AppCompatAutoCompleteTextView(context, attrs, defStyleAttr) {

    private val adapter = PopupAdapter(context)

    init {
        threshold = 1
        inputType = InputType.TYPE_NULL
        setAdapter(adapter)
        setOnTouchListener { _, _ ->
            showDropDown()
            false
        }
    }

    fun updateList(items: List<Nameable>) {
        adapter.clear()
        adapter.addAll("")
        if (items.isNotEmpty()) {
            adapter.addAll("Все")
        }
        adapter.addAll(items.map { it.name })
        adapter.notifyDataSetChanged()
    }
}

class PopupAdapter(context: Context) : ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, mutableListOf("")) {

    private val mThis get() = this

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()

                val mLock = ArrayAdapter::class.java.getDeclaredField("mLock")
                mLock.isAccessible = true
                val mOriginalValues = ArrayAdapter::class.java.getDeclaredField("mOriginalValues")
                mOriginalValues.isAccessible = true
                val mObjects = ArrayAdapter::class.java.getDeclaredField("mObjects")
                mObjects.isAccessible = true

                if (mOriginalValues.get(mThis) == null) {
                    synchronized(mLock.get(mThis)!!) {
                        mOriginalValues.set(mThis, mObjects.get(mThis))
                    }
                }
                val list: MutableList<*>
                synchronized(mLock.get(mThis)!!) {
                    list = ArrayList(mOriginalValues.get(mThis) as Collection<*>)
                }

                results.values = list
                results.count = list.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                val mObjects = ArrayAdapter::class.java.getDeclaredField("mObjects")
                mObjects.isAccessible = true
                mObjects.set(mThis, results.values)
                if (results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}
