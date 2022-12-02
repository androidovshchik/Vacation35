package rf.vacation35.screen

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.drop
import rf.vacation35.*
import rf.vacation35.databinding.FragmentBuildingBinding
import rf.vacation35.databinding.FragmentListBinding
import rf.vacation35.databinding.ItemBuildingBinding
import rf.vacation35.extension.*
import rf.vacation35.local.Preferences
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.Base
import rf.vacation35.remote.dao.Building
import splitties.fragments.start
import splitties.snackbar.snack
import javax.inject.Inject

@AndroidEntryPoint
class BuildingListActivity : AbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, BuildingListFragment(), false)
    }
}

@AndroidEntryPoint
class BuildingListFragment : AbstractFragment() {

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
            base.text = item.base!!.name
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
            val baseId = bbFragment.bases.value.single().id
            start<BuildingActivity> {
                putExtra(EXTRA_BASE_ID, baseId)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            bbFragment.bases.collect {
                val user = preferences.user!!
                binding.fabAdd.isVisible = it.size == 1 && user.admin
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            bbFragment.buildings.drop(1).collect {
                listJob?.cancel()
                listJob = launch {
                    childFragmentManager.with(R.id.fl_fullscreen, progress, {
                        val ids = bbFragment.buildings.value.map { it.id }
                        val items = withContext(Dispatchers.IO) {
                            api.listBuildings(ids)
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
            childFragmentManager.with(R.id.fl_fullscreen, bbProgress, {
                bbFragment.loadBuildings()
            }, {
                view?.snack(it)
            })
        }
    }

    override fun onDestroyView() {
        childFragmentManager.removeFragment(progress)
        childFragmentManager.removeFragment(bbProgress)
        super.onDestroyView()
    }
}

@AndroidEntryPoint
class BuildingActivity : AbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, BuildingFragment(), false)
    }
}

@AndroidEntryPoint
class BuildingFragment : AbstractFragment() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var preferences: Preferences

    private val progress = ProgressDialog()

    private lateinit var binding: FragmentBuildingBinding

    private var building: Building? = null

    private var findJob: Job? = null

    private val baseId get() = activity?.intent?.getIntExtra(EXTRA_BASE_ID, 0) ?: 0

    private val buildingId get() = activity?.intent?.getIntExtra(EXTRA_BUILDING_ID, 0) ?: 0

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
        binding.pColor.isEnabled = user.admin
        binding.etName.isFocusable = user.admin
        binding.tilEntry.isEndIconCheckable = user.admin
        binding.tilExit.isEndIconCheckable = user.admin
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
                val color = binding.pColor.mColor ?: throw Throwable("Не задан цвет")
                val name = binding.etName.value.ifEmpty { throw Throwable("Не задано имя") }
                val entry = binding.tilEntry.mTime ?: throw Throwable("Не задано время заезда")
                val exit = binding.tilExit.mTime ?: throw Throwable("Не задано время выезда")
                viewLifecycleOwner.lifecycleScope.launch {
                    childFragmentManager.with(R.id.fl_fullscreen, progress, {
                        withContext(Dispatchers.IO) {
                            if (building == null) {
                                building = api.create(Building) {
                                    it.base = Base.findById(baseId)!!
                                    it.name = name
                                    it.color = color
                                    it.entryTime = entry.toSecondOfDay()
                                    it.exitTime = exit.toSecondOfDay()
                                }
                            } else {
                                api.update(building!!) {
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
                        binding.pColor.mColor = it.color
                        binding.etName.setText(it.name)
                        binding.tilEntry.setTime(it.entryTime)
                        binding.tilExit.setTime(it.exitTime)
                        binding.btnBids.isEnabled = true
                        binding.btnBookings.isEnabled = true
                        binding.btnDelete.isEnabled = user.admin
                        binding.btnSave.isEnabled = user.admin || user.accessPrice
                    }
                    if (building == null) {
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
