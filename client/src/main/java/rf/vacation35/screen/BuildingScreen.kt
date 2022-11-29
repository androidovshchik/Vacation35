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
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.*
import rf.vacation35.databinding.FragmentBuildingBinding
import rf.vacation35.databinding.FragmentListBinding
import rf.vacation35.databinding.ItemBuildingBinding
import rf.vacation35.extension.*
import rf.vacation35.local.Preferences
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.Building
import splitties.fragments.start
import splitties.snackbar.snack
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class BuildingListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, BuildingListFragment(), false)
    }
}

@AndroidEntryPoint
class BuildingListFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var preferences: Preferences

    private val filter by lazy {  childFragmentManager.findFragmentById(R.id.f_filter) as FilterFragment }

    private val progress = ProgressDialog()

    private lateinit var binding: FragmentListBinding

    private var startJob: Job? = null

    private var listenJob: Job? = null

    @SuppressLint("SetTextI18n")
    private val adapter = abstractAdapter<ItemBuildingBinding, Building.Raw> {
        onCreateViewHolder { parent ->
            with(ItemBuildingBinding.inflate(layoutInflater, parent, false)) {
                AbstractAdapter.ViewHolder(this).apply {
                    root.setOnClickListener {
                        try {
                            val item = items[bindingAdapterPosition]
                            start<BuildingActivity> {
                                putExtra(EXTRA_ID, item.id)
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
            title = "Постройки"
            inflateNavMenu()
        }
        binding.rvList.adapter = adapter
        binding.fabAdd.setOnClickListener {
            start<BuildingActivity> {
                putExtra(EXTRA_BASE_ID, filter.bases.value)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val user = preferences.user!!
        binding.fabAdd.isVisible = user.admin
        listenJob?.cancel()
        startJob?.cancel()
        startJob = viewLifecycleOwner.lifecycleScope.launch {
            childFragmentManager.addFragment(R.id.fl_fullscreen, progress, false)
            childFragmentManager.with(R.id.fl_fullscreen, progress, {
                filter.loadBuildings()
                listenJob = launch {
                    filter.buildings.collect {
                        childFragmentManager.with(R.id.fl_fullscreen, progress, {
                            val ids = filter.buildings.value.map { it.id }
                            val items = withContext(Dispatchers.IO) {
                                api.listBuildings(ids)
                            }
                            adapter.items.clear()
                            adapter.items.addAll(items)
                            adapter.notifyDataSetChanged()
                        }, {
                            view?.snack(it)
                        })
                    }
                }
            }, {
                view?.snack(it)
            })
        }
    }

    private suspend fun loadBuildings() {
        val ids = filter.buildings.value.map { it.id }
        val items = withContext(Dispatchers.IO) {
            api.listBuildings(ids)
        }
        adapter.items.clear()
        adapter.items.addAll(items)
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        childFragmentManager.removeFragment(progress)
        super.onDestroyView()
    }
}

@AndroidEntryPoint
class BuildingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, BuildingFragment(), false)
    }
}

@AndroidEntryPoint
class BuildingFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var preferences: Preferences

    private val progress = ProgressDialog()

    private lateinit var binding: FragmentBuildingBinding

    private var building: Building? = null

    private var color: String? = null

    private var entry: LocalTime? = null

    private var exit: LocalTime? = null

    private var findJob: Job? = null

    private val buildingId get() = activity?.intent?.getIntExtra(EXTRA_ID, 0) ?: 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBuildingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val user = preferences.user!!
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = if (buildingId == 0) "Новая постройка" else "Постройка"
            inflateNavMenu()
        }
        binding.vColor.setOnClickListener {
            ColorPickerDialog.Builder(requireActivity())
                .setColorListener { value, hex ->
                    color = hex
                    binding.vColor.setBackgroundColor(value)
                }
                .customShow()
        }
        binding.tilEntry.isEndIconCheckable = buildingId <= 0 && user.admin
        binding.tilEntry.setEndIconOnClickListener {
            TimePickerDialog(requireActivity(), { _, hourOfDay, minute ->
                entry = LocalTime.of(hourOfDay, minute)
            }, entry?.hour ?: 0, entry?.minute ?: 0, true).show()
        }
        binding.tilExit.isEndIconCheckable = buildingId <= 0 && user.admin
        binding.tilExit.setEndIconOnClickListener {
            TimePickerDialog(requireActivity(), { _, hourOfDay, minute ->
                exit = LocalTime.of(hourOfDay, minute)
            }, exit?.hour ?: 0, exit?.minute ?: 0, true).show()
        }
        binding.btnBids.setOnClickListener {
            start<BookingListActivity> {
                putExtra(EXTRA_BUILDING_ID, building!!.id.value)
                putExtra(EXTRA_BIDS, true)
            }
        }
        binding.btnBookings.setOnClickListener {
            start<BookingListActivity> {
                putExtra(EXTRA_BUILDING_ID, building!!.id.value)
            }
        }
        binding.btnDelete.setOnClickListener {
            context?.areYouSure {
                viewLifecycleOwner.lifecycleScope.launch {
                    childFragmentManager.with(R.id.fl_fullscreen, progress, {
                        withContext(Dispatchers.IO) {
                            api.delete(building!!)
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
                            if (building == null) {
                                building = api.create(Building) {
                                    it.name = name
                                    it.color = color
                                    it.entryTime = entry
                                    it.exitTime = exit
                                }
                            } else {
                                api.update(building!!) {
                                    it.name = name
                                    it.color = color
                                    it.entryTime = entry
                                    it.exitTime = exit
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
        if (building != null || buildingId > 0) {
            findJob?.cancel()
            findJob = viewLifecycleOwner.lifecycleScope.launch {
                childFragmentManager.with(R.id.fl_fullscreen, progress, {
                    building = withContext(Dispatchers.IO) {
                        api.find(Building, building?.id?.value ?: buildingId)
                    }
                    building?.let {
                        val user = preferences.user!!
                        binding.vColor.setBackgroundColor(Color.parseColor(it.color))
                        binding.etName.setText(it.name)
                        binding.tilEntry.isEndIconCheckable = true
                        binding.etEntry.setText(it.entryTime?.let { time -> timeFormatter.format(time) })
                        binding.tilExit.isEndIconCheckable = true
                        binding.etExit.setText(it.exitTime?.let { time -> timeFormatter.format(time) })
                        binding.btnBids.isEnabled = true
                        binding.btnBookings.isEnabled = true
                        binding.btnDelete.isEnabled = user.admin
                        binding.btnSave.isEnabled = user.admin || user.accessPrice
                    }
                    if (building == null) {
                        binding.tilEntry.isEndIconCheckable = false
                        binding.tilExit.isEndIconCheckable = false
                        binding.btnBids.isEnabled = false
                        binding.btnBookings.isEnabled = false
                        binding.btnDelete.isEnabled = false
                        binding.btnSave.isEnabled = false
                        view?.snack("Постройка не найдена")
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
