package rf.vacation35.screen

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.drop
import rf.vacation35.*
import rf.vacation35.databinding.FragmentBookingBinding
import rf.vacation35.databinding.FragmentListBinding
import rf.vacation35.databinding.ItemBuildingBinding
import rf.vacation35.extension.*
import rf.vacation35.local.Preferences
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.Base
import rf.vacation35.remote.dao.Booking
import rf.vacation35.remote.dao.Building
import splitties.fragments.start
import splitties.snackbar.snack
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class BookingListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, BookingListFragment(), false)
    }
}

@AndroidEntryPoint
class BookingListFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var preferences: Preferences

    private val filter by lazy {  childFragmentManager.findFragmentById(R.id.f_filter) as FilterFragment }

    private val progress = ProgressDialog()

    private val filterProgress = ProgressDialog()

    private lateinit var binding: FragmentListBinding

    private var startJob: Job? = null

    private var listJob: Job? = null

    @SuppressLint("SetTextI18n")
    private val adapter = abstractAdapter<ItemBuildingBinding, Building.Raw> {
        onCreateViewHolder { parent ->
            with(ItemBuildingBinding.inflate(layoutInflater, parent, false)) {
                AbstractAdapter.ViewHolder(this).apply {
                    root.setOnClickListener {
                        try {
                            val item = items[bindingAdapterPosition]
                            start<BuildingActivity> {
                                putExtra(EXTRA_BUILDING_ID, item.id)
                            }
                        } catch (ignored: Throwable) {
                        }
                    }
                }
            }
        }
        onBindViewHolder { item ->
            color.setBackgroundColor(Color.parseColor(item.color))
            name.text = item.name
            base.text = item.base?.name.orEmpty()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = "Брони"
            inflateNavMenu()
        }
        binding.rvList.adapter = adapter
        binding.fabAdd.setOnClickListener {
            val baseId = filter.bases.value.first().id.value
            start<BuildingActivity> {
                putExtra(EXTRA_BASE_ID, baseId)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            filter.bases.collect {
                val user = preferences.user!!
                binding.fabAdd.isVisible = it.size == 1 && user.admin
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            filter.buildings.drop(1).collect {
                listJob?.cancel()
                listJob = launch {
                    childFragmentManager.with(R.id.fl_fullscreen, progress, {
                        val ids = filter.buildings.value.map { it.id }
                        val items = withContext(Dispatchers.IO) {
                            api.listBookings(ids)
                        }
                        adapter.items.clear()
                        adapter.items.addAll(items)
                        adapter.notifyDataSetChanged()
                    }, {
                        getView()?.snack(it)
                    })
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        startJob?.cancel()
        startJob = viewLifecycleOwner.lifecycleScope.launch {
            childFragmentManager.with(R.id.fl_fullscreen, filterProgress, {
                filter.loadBuildings()
            }, {
                view?.snack(it)
            })
        }
    }

    override fun onDestroyView() {
        childFragmentManager.removeFragment(progress)
        childFragmentManager.removeFragment(filterProgress)
        super.onDestroyView()
    }
}

@AndroidEntryPoint
class BookingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, BookingFragment(), false)
    }
}

@AndroidEntryPoint
class BookingFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var preferences: Preferences

    private val progress = ProgressDialog()

    private lateinit var binding: FragmentBookingBinding

    private var booking: Booking? = null

    private var entry: LocalDateTime? = null

    private var exit: LocalDateTime? = null

    private var findJob: Job? = null

    private val buildingId get() = activity?.intent?.getIntExtra(EXTRA_BUILDING_ID, 0) ?: 0

    private val bookingId get() = activity?.intent?.getLongExtra(EXTRA_BOOKING_ID, 0L) ?: 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val user = preferences.user!!
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = if (buildingId == 0) "Новая бронь" else "Бронь"
            inflateNavMenu()
        }
        binding.tilEntry.setEndIconOnClickListener {
            TimePickerDialog(requireActivity(), { _, hourOfDay, minute ->
                entry = LocalTime.of(hourOfDay, minute)
                binding.etEntry.setText(timeFormatter.format(entry))
            }, entry?.hour ?: 0, entry?.minute ?: 0, true).show()
        }
        binding.tilExit.setEndIconOnClickListener {
            TimePickerDialog(requireActivity(), { _, hourOfDay, minute ->
                exit = LocalTime.of(hourOfDay, minute)
                binding.etExit.setText(timeFormatter.format(exit))
            }, exit?.hour ?: 0, exit?.minute ?: 0, true).show()
        }
        binding.btnBids.setOnClickListener {
            start<BookingListActivity> {
                putExtra(EXTRA_BUILDING_ID, booking!!.id.value)
                putExtra(EXTRA_BIDS, true)
            }
        }
        binding.btnBookings.setOnClickListener {
            start<BookingListActivity> {
                putExtra(EXTRA_BUILDING_ID, booking!!.id.value)
            }
        }
        binding.btnDelete.setOnClickListener {
            context?.areYouSure {
                viewLifecycleOwner.lifecycleScope.launch {
                    childFragmentManager.with(R.id.fl_fullscreen, progress, {
                        withContext(Dispatchers.IO) {
                            api.delete(booking!!)
                        }
                        activity?.finish()
                    }, {
                        getView()?.snack(it)
                    })
                }
            }
        }
        binding.btnSave.isEnabled = buildingId <= 0 && (user.admin || user.accessPrice)
        binding.btnSave.setOnClickListener {
            try {
                val color = color ?: throw Throwable("Не задан цвет")
                val name = binding.etName.text.toString().trim().ifEmpty { throw Throwable("Не задано имя") }
                val entry = entry ?: throw Throwable("Не задано время заезда")
                val exit = exit ?: throw Throwable("Не задано время выезда")
                viewLifecycleOwner.lifecycleScope.launch {
                    childFragmentManager.with(R.id.fl_fullscreen, progress, {
                        withContext(Dispatchers.IO) {
                            if (booking == null) {
                                booking = api.create(Building) {
                                    it.base = Base.findById(baseId)!!
                                    it.name = name
                                    it.color = color
                                    it.entryTime = entry.toSecondOfDay()
                                    it.exitTime = exit.toSecondOfDay()
                                }
                            } else {
                                api.update(booking!!) {
                                    it.name = name
                                    it.color = color
                                    it.entryTime = entry.toSecondOfDay()
                                    it.exitTime = exit.toSecondOfDay()
                                }
                            }
                        }
                        @Suppress("NAME_SHADOWING")
                        val user = preferences.user!!
                        binding.toolbar.title = "Постройка"
                        binding.btnBids.isEnabled = true
                        binding.btnBookings.isEnabled = true
                        binding.btnDelete.isEnabled = user.admin
                    }, {
                        getView()?.snack(it)
                    })
                }
            } catch (e: Throwable) {
                getView()?.snack(e)
            }
        }
        context?.updateRecentColors()
    }

    override fun onStart() {
        super.onStart()
        if (booking != null || buildingId > 0) {
            findJob?.cancel()
            findJob = viewLifecycleOwner.lifecycleScope.launch {
                childFragmentManager.with(R.id.fl_fullscreen, progress, {
                    booking = withContext(Dispatchers.IO) {
                        api.find(Booking, booking?.id?.value ?: bookingId)
                    }
                    booking?.let {
                        val user = preferences.user!!
                        binding.vColor.setBackgroundColor(Color.parseColor(it.color))
                        binding.etName.setText(it.name)
                        binding.etEntry.setText(it.entryTime?.let { time -> timeFormatter.format(LocalTime.ofSecondOfDay(time.toLong())) })
                        binding.etExit.setText(it.exitTime?.let { time -> timeFormatter.format(LocalTime.ofSecondOfDay(time.toLong())) })
                        binding.btnBids.isEnabled = true
                        binding.btnBookings.isEnabled = true
                        binding.btnDelete.isEnabled = user.admin
                        binding.btnSave.isEnabled = user.admin || user.accessPrice
                    }
                    if (booking == null) {
                        binding.btnBids.isEnabled = false
                        binding.btnBookings.isEnabled = false
                        binding.btnDelete.isEnabled = false
                        binding.btnSave.isEnabled = false
                        view?.snack("Бронь не найдена")
                    }
                }, {
                    view?.snack(it)
                })
            }
        }
    }

    override fun onDestroyView() {
        childFragmentManager.removeFragment(progress)
        super.onDestroyView()
    }
}
