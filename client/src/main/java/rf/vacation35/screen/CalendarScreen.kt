package rf.vacation35.screen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import rf.vacation35.R
import rf.vacation35.databinding.FragmentCalendarBinding
import rf.vacation35.databinding.ItemMonthBinding
import rf.vacation35.extension.*
import rf.vacation35.remote.DbApi
import splitties.dimensions.dip
import splitties.fragments.start
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

    private lateinit var binding: FragmentCalendarBinding

    private var listJob: Job? = null

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
                adapter.mState = manager.onSaveInstanceState()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private val adapter = monthAdapter {
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
        val context = requireContext()
        val state = adapter.mState
        binding.rvCalendar.adapter = adapter
        if (state != null) {
            manager.onRestoreInstanceState(state)
        } else {
            manager.scrollToPositionWithOffset(2, context.dip(24))
        }
        binding.rvCalendar.addOnScrollListener(scrollListener)
        binding.fabAdd.setOnClickListener {
            start<BookingActivity>()
        }
        val filter = childFragmentManager.findFragmentById(R.id.f_filter) as FilterFragment
        viewLifecycleOwner.lifecycleScope.launch {
            filter.loadBuildings()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            filter.buildings.collect {

            }
        }
    }

    override fun onDestroyView() {
        childFragmentManager.removeFragment(progress)
        super.onDestroyView()
    }
}
