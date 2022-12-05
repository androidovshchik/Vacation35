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
import rf.vacation35.*
import rf.vacation35.databinding.FragmentBuildingBinding
import rf.vacation35.databinding.FragmentListBinding
import rf.vacation35.databinding.ItemBuildingBinding
import rf.vacation35.extension.*
import rf.vacation35.remote.dao.Base
import rf.vacation35.remote.dao.Building
import splitties.fragments.start
import splitties.snackbar.snack

@AndroidEntryPoint
class BuildingListActivity : AbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, BuildingListFragment(), false)
    }
}

@AndroidEntryPoint
class BuildingListFragment : AbstractFragment() {

    private val bbFragment by lazy { childFragmentManager.findFragmentById(R.id.f_bb) as BBHFragment }

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
            title = "Постройки"
            inflateNavMenu<BuildingListFragment>()
        }
        binding.rvList.adapter = adapter
        binding.fabAdd.setOnClickListener {
            try {
                val baseId = bbFragment.bases.value!!.single().id
                start<BuildingActivity> {
                    putExtra(EXTRA_BASE_ID, baseId)
                }
            } catch (ignored: Throwable) {
            }
        }
        bbFragment.bases.observe(viewLifecycleOwner) {
            val user = user.value
            binding.fabAdd.isVisible = it.size == 1 && user.admin
        }
        bbFragment.buildings.observe(viewLifecycleOwner) {
            loadBuildings()
        }
    }

    private fun loadBuildings() {
        listJob?.cancel()
        listJob = viewLifecycleOwner.lifecycleScope.launch {
            useProgress(::loadBuildings) {
                val ids = bbFragment.buildings.value?.map { it.id }.orEmpty()
                val items = withContext(Dispatchers.IO) {
                    api.listBuildings(ids)
                }
                adapter.items.clear()
                adapter.items.addAll(items)
                adapter.notifyDataSetChanged()
            }
        }
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

    private var building: Building? = null

    private lateinit var binding: FragmentBuildingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBuildingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = user.value
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = if (argBuildingId == 0) "Новая постройка" else "Постройка"
            inflateNavMenu<BuildingFragment>()
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
                delete()
            }
        }
        binding.btnSave.isEnabled = argBuildingId <= 0 && (user.admin || user.accessPrice)
        binding.btnSave.setOnClickListener {
            upsert()
        }
    }

    override fun readOnStart() {
        super.readOnStart()
        if (building == null && argBuildingId <= 0) {
            return
        }
        startJob?.cancel()
        startJob = viewLifecycleOwner.lifecycleScope.launch {
            useProgress(::readOnStart) {
                building = withContext(Dispatchers.IO) {
                    api.find(Building, building?.id?.value ?: argBuildingId)
                }
                building?.let {
                    val user = user.value
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
            }
        }
    }

    override fun upsert() {
        try {
            val color = binding.pColor.mColor ?: throw Throwable("Не задан цвет")
            val name = binding.etName.value.ifEmpty { throw Throwable("Не задано имя") }
            val entry = binding.tilEntry.mTime ?: throw Throwable("Не задано время заезда")
            val exit = binding.tilExit.mTime ?: throw Throwable("Не задано время выезда")
            viewLifecycleOwner.lifecycleScope.launch {
                useProgress(::upsert) {
                    withContext(Dispatchers.IO) {
                        if (building == null) {
                            building = api.insert(Building) {
                                it.base = Base.findById(argBaseId)!!
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
                    val user = user.value
                    binding.toolbar.title = "Постройка"
                    binding.btnBids.isEnabled = true
                    binding.btnBookings.isEnabled = true
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
                    api.delete(building!!)
                }
                activity?.finish()
            }
        }
    }
}
