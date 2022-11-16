package rf.vacation35.screen

import android.os.Parcelable
import rf.vacation35.databinding.ItemMonthBinding
import rf.vacation35.extension.minus
import rf.vacation35.extension.plus
import rf.vacation35.remote.dao.BookingDao
import java.time.LocalDate
import java.time.YearMonth

inline fun monthAdapter(body: MonthAdapter.() -> Unit): MonthAdapter {
    return MonthAdapter().apply { body() }
}

class MonthAdapter : AbstractAdapter<ItemMonthBinding, YearMonth>(mutableListOf()) {

    val mBookings = mutableListOf<BookingDao>()

    var mState: Parcelable? = null

    init {
        val month = YearMonth.now()
        items.add(month - 2)
        items.add(month - 1)
        items.add(month)
        items.add(month + 1)
        items.add(month + 2)

        setHasStableIds(true)
    }

    fun insertFirst() {
        val first = items.firstOrNull()?.takeIf { it.year > LocalDate.MIN.year } ?: return
        items.add(0, first - 1)
        notifyItemInserted(0)
    }

    fun insertLast() {
        val last = items.lastOrNull() ?: return
        items.add(last + 1)
        notifyItemInserted(items.lastIndex)
    }

    override fun onBindViewHolder(holder: ViewHolder<ItemMonthBinding>, position: Int) {
        val item = items[position]
        holder.binding.ml.notify(item, mBookings)
    }

    /** @see https://en.wikipedia.org/wiki/Pairing_function */
    override fun getItemId(position: Int): Long {
        val item = items[position]
        val a = item.year
        val b = item.monthValue
        return ((a + b) * (a + b + 1) / 2 + b).toLong()
    }
}
