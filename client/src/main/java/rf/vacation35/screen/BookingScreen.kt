package rf.vacation35.screen

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mohamedabulgasem.datetimepicker.DateTimePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectIndexed
import rf.vacation35.*
import rf.vacation35.databinding.FragmentBookingBinding
import rf.vacation35.databinding.FragmentListBinding
import rf.vacation35.databinding.ItemBookingBinding
import rf.vacation35.extension.*
import rf.vacation35.local.Preferences
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.Booking
import rf.vacation35.remote.dao.Building
import splitties.fragments.start
import splitties.snackbar.snack
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
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

    private val progress = ProgressDialog()

    private val bbProgress = ProgressDialog()

    private val bbFragment by lazy { childFragmentManager.findFragmentById(R.id.f_bb) as BBHFragment }

    private lateinit var binding: FragmentListBinding

    private var startJob: Job? = null

    private var listJob: Job? = null

    private lateinit var end: LocalDate

    private val start get() = end.minusMonths(5)

    private val scrollListener = object : EndlessListener() {

        override fun onScrolledToTop() {
        }

        override fun onScrolledToBottom() {
            end = start
            loadBookings()
        }
    }

    @SuppressLint("SetTextI18n")
    private val adapter = abstractAdapter<ItemBookingBinding, Booking.Raw> {
        onCreateViewHolder { parent ->
            with(ItemBookingBinding.inflate(layoutInflater, parent, false)) {
                AbstractAdapter.ViewHolder(this).apply {
                    root.setOnClickListener {
                        try {
                            val item = items[bindingAdapterPosition]
                            start<BookingActivity> {
                                putExtra(EXTRA_BUILDING_ID, item.building?.id ?: 0)
                                putExtra(EXTRA_BOOKING_ID, item.id)
                            }
                        } catch (ignored: Throwable) {
                        }
                    }
                }
            }
        }
        onBindViewHolder { item ->
            if (item.bid) {
                root.setBackgroundColor(0xff616161.toInt())
                times.setTextColor(Color.WHITE)
            } else {
                root.setBackgroundColor(0)
                times.setTextColor(Color.BLACK)
            }
            color.setBackgroundColor(Color.parseColor(item.building?.color))
            times.text = "${shortDateTimeFormatter.format(item.start)} - ${shortDateTimeFormatter.format(item.endInclusive)}"
            building.text = item.building?.name.orEmpty()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        end = activity?.intent?.getSerializableExtra(EXTRA_DATE) as LocalDate? ?: LocalDate.now()
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
        binding.rvList.addOnScrollListener(scrollListener)
        binding.fabAdd.setOnClickListener {
            val baseId = bbFragment.bases.value.singleOrNull()?.id
            val buildingId = bbFragment.buildings.value.single().id
            start<BookingActivity> {
                if (baseId != null) {
                    putExtra(EXTRA_BASE_ID, baseId)
                }
                putExtra(EXTRA_BUILDING_ID, buildingId)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            bbFragment.buildings.collectIndexed { i, buildings ->
                val user = preferences.user!!
                binding.fabAdd.isVisible = buildings.size == 1 && (user.admin || user.accessBooking)
                if (i > 0) {
                    listJob?.cancel()
                    listJob = launch {
                        childFragmentManager.with(R.id.fl_fullscreen, progress, {
                            val ids = bbFragment.buildings.value.map { it.id }
                            val items = withContext(Dispatchers.IO) {
                                api.listBookings(ids, start, end)
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
    }

    override fun onStart() {
        super.onStart()
        startJob?.cancel()
        startJob = viewLifecycleOwner.lifecycleScope.launch {
            childFragmentManager.with(R.id.fl_fullscreen, bbProgress, {
                bbFragment.loadBuildings()
            }, {
                view?.snack(it)
            })
        }
    }

    private fun loadBookings() {
        listJob?.cancel()
        listJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val ids = bbFragment.buildings.value.map { it.id }
                val items = withContext(Dispatchers.IO) {
                    api.listBookings(ids, start, end)
                }
                adapter.items.clear()
                adapter.items.addAll(items)
                adapter.notifyDataSetChanged()
            } finally {
                binding.pbLoading.isVisible = false
            }
        }
    }

    override fun onDestroyView() {
        childFragmentManager.removeFragment(progress)
        childFragmentManager.removeFragment(bbProgress)
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

    private var buildingId = 0

    private val bookingId get() = activity?.intent?.getLongExtra(EXTRA_BOOKING_ID, 0L) ?: 0L

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        buildingId = activity?.intent?.getIntExtra(EXTRA_BUILDING_ID, 0) ?: 0
    }

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
            DateTimePicker.Builder(requireActivity())
                .initialValues(entry?.year, entry?.monthValue, entry?.dayOfMonth, entry?.hour, entry?.minute)
                .onDateTimeSetListener { year, month, dayOfMonth, hourOfDay, minute ->
                    entry = LocalDateTime.of(year, month, dayOfMonth, hourOfDay, minute)
                    binding.etEntry.setText(dateTimeFormatter.format(entry))
                }
                .build()
                .show()
        }
        binding.tilExit.setEndIconOnClickListener {
            DateTimePicker.Builder(requireActivity())
                .initialValues(exit?.year, exit?.monthValue, exit?.dayOfMonth, exit?.hour, exit?.minute)
                .onDateTimeSetListener { year, month, dayOfMonth, hourOfDay, minute ->
                    exit = LocalDateTime.of(year, month, dayOfMonth, hourOfDay, minute)
                    binding.etEntry.setText(dateTimeFormatter.format(exit))
                }
                .build()
                .show()
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
                val buildingId = buildingId.takeIf { it > 0 } ?: throw Throwable("Не выбрана постройка")
                val entry = entry ?: throw Throwable("Не задано время заезда")
                val exit = exit ?: throw Throwable("Не задано время выезда")
                val clientName = binding.etClientName.text.toString().trim().ifEmpty { throw Throwable("Не задано имя клиента") }
                val phone = binding.etPhone.text.toString().trim().ifEmpty { throw Throwable("Не задан телефон") }
                viewLifecycleOwner.lifecycleScope.launch {
                    childFragmentManager.with(R.id.fl_fullscreen, progress, {
                        withContext(Dispatchers.IO) {
                            if (booking == null) {
                                booking = api.create(Booking) {
                                    it.building = Building.findById(buildingId)!!
                                    it.entryTime = entry.toEpochSecond(ZoneOffset.UTC)
                                    it.exitTime = exit.toEpochSecond(ZoneOffset.UTC)
                                    it.clientName = clientName
                                    it.phone = phone
                                }
                            } else {
                                api.update(booking!!) {
                                    it.entryTime = entry.toEpochSecond(ZoneOffset.UTC)
                                    it.exitTime = exit.toEpochSecond(ZoneOffset.UTC)
                                    it.clientName = clientName
                                    it.phone = phone
                                }
                            }
                        }
                        @Suppress("NAME_SHADOWING")
                        val user = preferences.user!!
                        binding.toolbar.title = "Бронь"
                        binding.btnDelete.isEnabled = user.admin || user.accessBooking
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
        if (booking != null || bookingId > 0) {
            findJob?.cancel()
            findJob = viewLifecycleOwner.lifecycleScope.launch {
                childFragmentManager.with(R.id.fl_fullscreen, progress, {
                    booking = withContext(Dispatchers.IO) {
                        api.find(Booking, booking?.id?.value ?: bookingId)
                    }
                    booking?.let {
                        val user = preferences.user!!
                        binding.etEntry.setText(timeFormatter.format(LocalDateTime.ofEpochSecond(it.entryTime, 0, ZoneOffset.UTC)))
                        binding.etExit.setText(timeFormatter.format(LocalDateTime.ofEpochSecond(it.exitTime, 0, ZoneOffset.UTC)))
                        binding.etClientName.setText(it.clientName)
                        binding.etPhone.setText(it.phone)
                        binding.btnDelete.isEnabled = user.admin || user.accessBooking
                        binding.btnSave.isEnabled = user.admin || user.accessBooking
                    }
                    if (booking == null) {
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
