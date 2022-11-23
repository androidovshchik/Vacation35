package rf.vacation35.screen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.R
import rf.vacation35.databinding.FragmentCalendarBinding
import rf.vacation35.databinding.ItemMonthBinding
import rf.vacation35.extension.*
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.Booking
import splitties.dimensions.dip
import splitties.fragments.start
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import javax.inject.Inject

@AndroidEntryPoint
class CalendarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, CalendarFragment(), false)
    }
}

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    private val progress = ProgressDialog()

    private val filter by lazy {  childFragmentManager.findFragmentById(R.id.f_filter) as FilterFragment }

    private lateinit var binding: FragmentCalendarBinding

    private var queryJob: Job? = null

    private var scrolledMonth = YearMonth.now()

    private val manager: LinearLayoutManager
        get() = binding.rvCalendar.layoutManager as LinearLayoutManager

    private val scrollListener = object : EndlessListener() {

        override fun onScrolledToTop() {
            adapter.insertFirst()
        }

        override fun onScrolledToBottom() {
            adapter.insertLast()
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                try {
                    val position = manager.findFirstVisibleItemPosition()
                    val month = adapter.items[position] + 1
                    if (month !in scrolledMonth - 3..scrolledMonth + 2) {
                        scrolledMonth = month
                        loadBookings()
                    }
                } catch (ignored: Throwable) {
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private val adapter = MonthAdapter().apply {
        onCreateViewHolder { parent ->
            with(ItemMonthBinding.inflate(layoutInflater, parent, false)) {
                AbstractAdapter.ViewHolder(this).apply {
                    root.setOnClickListener {
                        try {
                            val item = items[bindingAdapterPosition]

                        } catch (ignored: Throwable) {
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvCalendar.adapter = adapter
        scrollToToday()
        binding.rvCalendar.addOnScrollListener(scrollListener)
        binding.fabAdd.setOnClickListener {
            start<BookingActivity>()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            childFragmentManager.with(R.id.fl_fullscreen, progress, {
                filter.loadBuildings()
            }, {
                getView()?.snack(it)
            })
        }
        viewLifecycleOwner.lifecycleScope.launch {
            filter.buildings.collect {
                loadBookings()
            }
        }
    }

    fun scrollToToday() {
        binding.rvCalendar.stopScroll()
        val context = context ?: return
        val position = adapter.items.indexOf(YearMonth.now())
        manager.scrollToPositionWithOffset(position, context.dip(24))
    }

    private fun loadBookings() {
        queryJob?.cancel()
        binding.pbLoading.isVisible = true
        queryJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val buildingIds = filter.buildings.value.map { it.id }
                val start = LocalDateTime.of((scrolledMonth - 3).atDay(1), LocalTime.MIN)
                val end = LocalDateTime.of((scrolledMonth + 2).atEndOfMonth(), LocalTime.MAX)
                val bookings = withContext(Dispatchers.IO) {
                    api.queryBookings(buildingIds, start, end)
                }
                adapter.mBookings.clear()
                adapter.mBookings.addAll(bookings)
                adapter.notifyDataSetChanged()
            } finally {
                binding.pbLoading.isVisible = false
            }
        }
    }

    override fun onDestroyView() {
        childFragmentManager.removeFragment(progress)
        super.onDestroyView()
    }
}

class MonthAdapter : AbstractAdapter<ItemMonthBinding, YearMonth>(mutableListOf()) {

    val mBookings = mutableListOf<Booking.Raw>()

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
        holder.binding.ml.update(item)
        holder.binding.ml.update(mBookings, true)
    }

    /** @see https://en.wikipedia.org/wiki/Pairing_function */
    override fun getItemId(position: Int): Long {
        val item = items[position]
        val a = item.year
        val b = item.monthValue
        return ((a + b) * (a + b + 1) / 2 + b).toLong()
    }
}
