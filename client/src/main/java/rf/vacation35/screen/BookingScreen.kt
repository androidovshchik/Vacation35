package rf.vacation35.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.redmadrobot.inputmask.MaskedTextChangedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import rf.vacation35.*
import rf.vacation35.databinding.FragmentBookingBinding
import rf.vacation35.databinding.FragmentListBinding
import rf.vacation35.databinding.ItemBookingBinding
import rf.vacation35.extension.*
import rf.vacation35.remote.dao.Booking
import rf.vacation35.remote.dao.Building
import rf.vacation35.remote.dao.User
import splitties.fragments.start
import splitties.snackbar.snack
import java.time.*

@AndroidEntryPoint
class BookingListActivity : AbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, BookingListFragment(), false)
    }
}

@AndroidEntryPoint
class BookingListFragment : AbstractFragment() {

    private val bbFragment by lazy { childFragmentManager.findFragmentById(R.id.f_bb) as BBHFragment }

    private var listJob: Job? = null

    private var endDay = LocalDate.now()

    private val startDay get() = endDay.minusMonths(5)

    private val scrollListener = object : EndlessListener() {

        override fun onScrolledToTop() {
        }

        override fun onScrolledToBottom() {
            endDay = startDay
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
                root.setCardBackgroundColor(0xff616161.toInt())
                times.setTextColor(Color.WHITE)
            } else {
                root.setCardBackgroundColor(0)
                times.setTextColor(Color.BLACK)
            }
            color.setBackgroundColor(Color.parseColor(item.building?.color))
            times.text = "${shortDateTimeFormatter.format(item.start)} - ${shortDateTimeFormatter.format(item.endInclusive)}"
            building.text = item.building?.name.orEmpty()
        }
    }

    private lateinit var binding: FragmentListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = when {
                argBids == true -> "Заявки"
                argDate != null -> "Брони на ${dateFormatter.format(argDate)}"
                else -> "Брони"
            }
            inflateNavMenu(mThis)
        }
        binding.rvList.adapter = adapter
        if (argDate == null) {
            binding.rvList.addOnScrollListener(scrollListener)
        }
        binding.fabAdd.setOnClickListener {
            val baseId = bbFragment.bases.value.singleOrNull()?.id ?: 0
            val buildingId = bbFragment.buildings.value.singleOrNull()?.id ?: 0
            start<BookingActivity> {
                putExtra(EXTRA_BASE_ID, baseId)
                putExtra(EXTRA_BUILDING_ID, buildingId)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            bbFragment.buildings.collect {
                loadBookings()
            }
        }
    }

    override fun onUserChanged(user: User.Raw) {
        binding.fabAdd.isVisible = user.admin || user.accessBooking
    }

    private fun loadBookings() {
        listJob?.cancel()
        listJob = viewLifecycleOwner.lifecycleScope.launch {
            useProgress(::loadBookings) {
                val ids = bbFragment.buildings.value.map { it.id }
                val items = mutableSetOf<Booking.Raw>()
                items.addAll(adapter.items)
                items.addAll(withContext(Dispatchers.IO) {
                    if (argDate != null) {
                        api.listBookings(ids, argDate!!, argDate!!, argBids)
                    } else {
                        api.listBookings(ids, startDay, endDay, argBids)
                    }
                })
                adapter.items.clear()
                adapter.items.addAll(items)
                adapter.notifyDataSetChanged()
            }
        }
    }
}

@AndroidEntryPoint
class BookingActivity : AbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, BookingFragment(), false)
    }
}

@AndroidEntryPoint
class BookingFragment : AbstractFragment() {

    private val bbFragment by lazy { childFragmentManager.findFragmentById(R.id.f_bb) as BBHFragment }

    private lateinit var binding: FragmentBookingBinding

    private var booking: Booking? = null

    private val bookingId get() = activity?.intent?.getLongExtra(EXTRA_BOOKING_ID, 0L) ?: 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = user.value
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = if (bookingId == 0L) "Новая бронь" else "Бронь"
            inflateNavMenu(mThis)
        }
        binding.fabPhone.setOnClickListener {
            try {
                val phone = binding.etPhone.value
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$phone")
                startActivity(intent)
            } catch (e: Throwable) {
                getView()?.snack(e)
            }
        }
        binding.btnDelete.setOnClickListener {
            context?.areYouSure {
                delete()
            }
        }
        MaskedTextChangedListener.installOn(binding.etPhone, "+7([000]) [000]-[00]-[00]")
        binding.btnSave.isEnabled = bookingId <= 0 && (user.admin || user.accessPrice)
        binding.btnSave.setOnClickListener {
            upsert()
        }
    }

    override fun readOnStart() {
        super.readOnStart()
        if (booking == null && argBookingId <= 0) {
            return
        }
        startJob?.cancel()
        startJob = viewLifecycleOwner.lifecycleScope.launch {
            useProgress(::readOnStart) {
                var buildingId: Int? = null
                booking = withContext(Dispatchers.IO) {
                    api.find(Booking, booking?.id?.value ?: bookingId) {
                        buildingId = it?.building?.id?.value
                    }
                }
                bbFragment.selectAll(buildingId = buildingId ?: 0)
                booking?.let {
                    val user = user.value
                    binding.dilEntry.setDate(it.entryTime)
                    binding.dilExit.setDate(it.exitTime)
                    binding.etClientName.setText(it.clientName)
                    binding.etPhone.setText(it.phone)
                    binding.sActive.isChecked = !it.bid
                    binding.btnDelete.isEnabled = user.admin
                    binding.btnSave.isEnabled = user.admin || user.accessBooking
                }
                if (booking == null) {
                    binding.btnDelete.isEnabled = false
                    binding.btnSave.isEnabled = false
                    view?.snack("Бронь не найдена")
                }
            }
        }
    }

    override fun upsert() {
        try {
            val buildingId = bbFragment.buildings.value.singleOrNull()?.id ?: throw Throwable("Не выбрана постройка")
            val entry = binding.dilEntry.mDate ?: throw Throwable("Не задано время заезда")
            val exit = binding.dilExit.mDate ?: throw Throwable("Не задано время выезда")
            val clientName = binding.etClientName.value.ifEmpty { throw Throwable("Не задано имя клиента") }
            val phone = binding.etPhone.value.ifEmpty { throw Throwable("Не задан телефон") }
            val bid = !binding.sActive.isChecked
            viewLifecycleOwner.lifecycleScope.launch {
                useProgress(::upsert) {
                    withContext(Dispatchers.IO) {
                        if (booking == null) {
                            booking = api.insert(Booking) {
                                it.building = Building.findById(buildingId)!!
                                it.entryTime = entry.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
                                it.exitTime = exit.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
                                it.clientName = clientName
                                it.phone = phone
                                it.bid = bid
                            }
                        } else {
                            api.update(booking!!) {
                                it.entryTime = entry.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
                                it.exitTime = exit.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
                                it.clientName = clientName
                                it.phone = phone
                                it.bid = bid
                            }
                        }
                    }
                    val user = user.value
                    binding.toolbar.title = "Бронь"
                    binding.btnDelete.isEnabled = user.admin
                }
            }
        } catch (e: Throwable) {
            view?.snack(e)
        }
    }

    override fun delete() {
        viewLifecycleOwner.lifecycleScope.launch {
            useProgress(::delete) {
                withContext(Dispatchers.IO) {
                    api.delete(booking!!)
                }
                activity?.finish()
            }
        }
    }
}
