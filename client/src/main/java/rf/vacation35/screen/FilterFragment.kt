package rf.vacation35.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.EXTRA_BASE_ID
import rf.vacation35.EXTRA_BASE_RAW
import rf.vacation35.EXTRA_BUILDING_ID
import rf.vacation35.EXTRA_BUILDING_RAW
import rf.vacation35.databinding.FragmentFilterBinding
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.Base
import rf.vacation35.remote.dao.Building
import rf.vacation35.remote.dao.Nameable
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.max

@AndroidEntryPoint
class FilterFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    val bases = MutableStateFlow(listOf<Base>())

    val buildings = MutableStateFlow(listOf<Building.Raw>())

    private val allBases = MutableStateFlow(listOf<Base>())

    private val allBuildings = mutableListOf<Building.Raw>()

    private lateinit var binding: FragmentFilterBinding

    private val filteredBuildings: List<Building.Raw> get() {
        val baseIds = bases.value.map { it.id.value }
        return allBuildings.filter { it.base?.id in baseIds }
    }

    private val baseId get() = activity?.intent?.getParcelableExtra<Base.Raw>(EXTRA_BASE_RAW)?.id
        ?: activity?.intent?.getIntExtra(EXTRA_BASE_ID, 0)
        ?: 0

    private val buildingId get() = activity?.intent?.getParcelableExtra<Building.Raw>(EXTRA_BUILDING_RAW)?.id
        ?: activity?.intent?.getIntExtra(EXTRA_BUILDING_ID, 0)
        ?: 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.esBase.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    bases.value = emptyList()
                    selectBuildings(items = emptyList())
                }
                1 -> {
                    bases.value = allBases.value
                    selectAllBuildings(allBuildings)
                }
                else -> {
                    val base = allBases.value.getOrNull(max(0, position - 2))
                    if (base != null) {
                        bases.value = listOf(base)
                        selectAllBuildings(filteredBuildings)
                    }
                }
            }
        }
        binding.esBuilding.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> buildings.value = emptyList()
                1 -> buildings.value = filteredBuildings
                else -> {
                    val building = filteredBuildings.getOrNull(max(0, position - 2))
                    if (building != null) {
                        buildings.value = listOf(building)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            allBases.drop(1).collectIndexed { i, items ->
                if (i == 0) {
                    val baseId = baseId
                    val base = items.firstOrNull { it.id.value == baseId }
                    if (base != null) {
                        selectBases(base, items)
                    } else {
                        selectAllBases()
                    }
                    val buildingId = buildingId
                    val building = allBuildings.firstOrNull { it.id == buildingId }
                    when {
                        building != null && building.base?.id == baseId -> selectBuildings(building, items = allBuildings)
                        base != null -> selectAllBuildings(filteredBuildings)
                        else -> selectAllBuildings(allBuildings)
                    }
                } else {
                    binding.esBase.updateList(items)
                    binding.esBuilding.updateList(filteredBuildings)
                }
            }
        }
    }

    private fun selectBases(value: Base? = null, items: List<Nameable>) {
        bases.value = if (value != null) listOf(value) else emptyList()
        binding.esBase.updateList(items)
        binding.esBase.setText(value?.name.orEmpty())
    }

    private fun selectAllBases() {
        bases.value = allBases.value
        binding.esBase.updateList(allBases.value)
        binding.esBase.setText("Все")
    }

    private fun selectBuildings(value: Building.Raw? = null, items: List<Nameable>) {
        buildings.value = if (value != null) listOf(value) else emptyList()
        binding.esBuilding.updateList(items)
        binding.esBuilding.setText(value?.name.orEmpty())
    }

    private fun selectAllBuildings(items: List<Building.Raw>) {
        buildings.value = items
        binding.esBuilding.updateList(items)
        binding.esBuilding.setText("Все")
    }

    suspend fun loadBuildings() {
        withContext(Dispatchers.IO) {
            while (true) {
                try {
                    allBuildings.clear()
                    allBuildings.addAll(withContext(Dispatchers.IO) {
                        DbApi.getInstance()
                            .listBuildings()
                            .sortedBy { it.name }
                    })
                    allBases.value = withContext(Dispatchers.IO) {
                        DbApi.getInstance()
                            .list(Base)
                            .sortedBy { it.name }
                    }
                    break
                } catch (e: Throwable) {
                    Timber.e(e)
                    delay(10_000L)
                }
            }
        }
    }
}
