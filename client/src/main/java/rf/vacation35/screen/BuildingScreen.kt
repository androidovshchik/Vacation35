package rf.vacation35.screen

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.EXTRA_ID
import rf.vacation35.R
import rf.vacation35.databinding.FragmentBuildingBinding
import rf.vacation35.databinding.FragmentListBinding
import rf.vacation35.databinding.ItemBuildingBinding
import rf.vacation35.extension.*
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

    private val filter by lazy {  childFragmentManager.findFragmentById(R.id.f_filter) as FilterFragment }

    private val progress = ProgressDialog()

    private lateinit var binding: FragmentListBinding

    private var startJob: Job? = null

    private var listJob: Job? = null

    private val baseId get() = activity?.intent?.getIntExtra(EXTRA_ID, 0) ?: 0

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
            base.text = item.base.name
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
                putExtra(EXTRA_ID, baseId)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            filter.buildings.drop(1).collect {
                loadBuildings()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        startJob?.cancel()
        startJob = viewLifecycleOwner.lifecycleScope.launch {
            childFragmentManager.with(R.id.fl_fullscreen, progress, {
                if (baseId > 0) {
                    val items = withContext(Dispatchers.IO) {
                        api.listBuildings(baseId)
                    }
                    adapter.items.clear()
                    adapter.items.addAll(items)
                    adapter.notifyDataSetChanged()
                } else {
                    filter.loadBuildings()
                    if (!filter.selectInitially()) {
                        loadBuildings()
                    }
                }
            }, {
                view?.snack(it)
            })
        }
    }

    private fun loadBuildings() {
        val ids = filter.buildings.value.map { it.id }
        listJob?.cancel()
        listJob = viewLifecycleOwner.lifecycleScope.launch {
            childFragmentManager.with(R.id.fl_fullscreen, progress, {
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

    private val progress = ProgressDialog()

    private lateinit var binding: FragmentBuildingBinding

    private var building: Building? = null

    private var color: String? = null

    private var entry: LocalTime? = null

    private var exit: LocalTime? = null

    private var findJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBuildingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = activity?.intent?.getIntExtra(EXTRA_ID, 0) ?: 0
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = if (id == 0) "Новая постройка" else "Постройка"
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
        binding.tilEntry.isEndIconCheckable = id <= 0
        binding.tilEntry.setEndIconOnClickListener {
            TimePickerDialog(requireActivity(), { _, hourOfDay, minute ->
                entry = LocalTime.of(hourOfDay, minute)
            }, entry?.hour ?: 0, entry?.minute ?: 0, true).show()
        }
        binding.tilExit.isEndIconCheckable = id <= 0
        binding.tilExit.setEndIconOnClickListener {
            TimePickerDialog(requireActivity(), { _, hourOfDay, minute ->
                exit = LocalTime.of(hourOfDay, minute)
            }, exit?.hour ?: 0, exit?.minute ?: 0, true).show()
        }
        binding.btnBids.setOnClickListener {

        }
        binding.btnBookings.setOnClickListener {

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
        binding.btnSave.isEnabled = id <= 0
        binding.btnSave.setOnClickListener {
            try {
                val name = binding.etName.text.toString().trim().ifEmpty { throw Throwable("Не задано имя") }
                val color = color ?: throw Throwable("Не задан цвет")
                viewLifecycleOwner.lifecycleScope.launch {
                    childFragmentManager.with(R.id.fl_fullscreen, progress, {
                        withContext(Dispatchers.IO) {
                            if (building == null) {
                                building = api.create(Building) {
                                    it.name = name
                                    it.color = name
                                }
                            } else {
                                api.update(building!!) {
                                    it.name = name
                                }
                            }
                        }
                        binding.toolbar.title = "Постройка"
                        binding.btnDelete.isEnabled = true
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
        val id = activity?.intent?.getIntExtra(EXTRA_ID, 0) ?: 0
        if (id > 0 || building != null) {
            findJob?.cancel()
            findJob = viewLifecycleOwner.lifecycleScope.launch {
                childFragmentManager.with(R.id.fl_fullscreen, progress, {
                    building = withContext(Dispatchers.IO) {
                        api.find(Building, id)
                    }
                    building?.let {
                        binding.etName.setText(it.name)
                        binding.vColor.setBackgroundColor(Color.parseColor(it.color))
                        binding.btnDelete.isEnabled = true
                        binding.btnSave.isEnabled = true
                    }
                    if (building == null) {
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
