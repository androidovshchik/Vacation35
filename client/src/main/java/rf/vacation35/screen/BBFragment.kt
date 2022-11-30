package rf.vacation35.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.EXTRA_BASE_ID
import rf.vacation35.EXTRA_BUILDING_ID
import rf.vacation35.databinding.FragmentBbhBinding
import rf.vacation35.databinding.FragmentBbvBinding
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.Base
import rf.vacation35.remote.dao.Building
import javax.inject.Inject
import kotlin.math.max

@AndroidEntryPoint
class BBVFragment : BBHFragment() {

    override val baseSpinner get() = binding.esBase

    override val buildingSpinner get() = binding.esBuilding

    private lateinit var binding: FragmentBbvBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBbvBinding.inflate(inflater, container, false)
        return binding.root
    }
}

@AndroidEntryPoint
open class BBHFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    val bases = MutableStateFlow(listOf<Base.Raw>())

    val buildings = MutableStateFlow(listOf<Building.Raw>())

    private val allBases = MutableSharedFlow<List<Base.Raw>>()

    private val allBasesValue = mutableListOf<Base.Raw>()

    private val allBuildings = mutableListOf<Building.Raw>()

    private val filteredBuildings: List<Building.Raw> get() {
        val baseIds = bases.value.map { it.id }
        return allBuildings.filter { it.base!!.id in baseIds }
    }

    private val baseId get() = activity?.intent?.getIntExtra(EXTRA_BASE_ID, 0) ?: 0

    private val buildingId get() = activity?.intent?.getIntExtra(EXTRA_BUILDING_ID, 0) ?: 0

    protected open val baseSpinner get() = binding.esBase

    protected open val buildingSpinner get() = binding.esBuilding

    private lateinit var binding: FragmentBbhBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBbhBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        baseSpinner.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    bases.value = emptyList()
                    selectBuildings(emptyList())
                }
                1 -> {
                    bases.value = allBasesValue
                    selectBuildings(allBuildings)
                }
                else -> {
                    val base = allBasesValue.getOrNull(max(0, position - 2))
                    if (base != null) {
                        bases.value = listOf(base)
                        selectBuildings(filteredBuildings)
                    }
                }
            }
        }
        buildingSpinner.setOnItemClickListener { _, _, position, _ ->
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
            allBases.collectIndexed { i, items ->
                if (i == 0) {
                    var baseId = baseId
                    var base = items.firstOrNull { it.id == baseId }
                    val buildingId = buildingId
                    val building = allBuildings.firstOrNull { it.id == buildingId }
                    if (base == null && building != null) {
                        baseId = building.base!!.id
                        base = items.firstOrNull { it.id == baseId }
                    }
                    if (base != null) {
                        selectBases(listOf(base))
                    } else {
                        selectBases(allBasesValue)
                    }
                    when {
                        building?.base?.id == baseId -> selectBuildings(listOf(building))
                        base != null -> selectBuildings(filteredBuildings)
                        else -> selectBuildings(allBuildings)
                    }
                } else {
                    val baseIds = items.map { it.id }
                    selectBases(bases.value.filter { it.id in baseIds })
                    selectBuildings(filteredBuildings)
                }
            }
        }
    }

    private fun selectBases(value: List<Base.Raw>) {
        bases.value = value
        baseSpinner.updateList(allBasesValue)
        baseSpinner.setText(when(value.size) {
            0 -> ""
            1 -> value.first().name
            else -> "Все"
        })
        baseSpinner.clearFocus()
    }

    private fun selectBuildings(value: List<Building.Raw>) {
        buildings.value = value
        buildingSpinner.updateList(filteredBuildings)
        buildingSpinner.setText(when(value.size) {
            0 -> ""
            1 -> value.first().name
            else -> "Все"
        })
        buildingSpinner.clearFocus()
    }

    suspend fun loadBuildings() {
        withContext(Dispatchers.IO) {
            allBuildings.clear()
            allBuildings.addAll(withContext(Dispatchers.IO) {
                DbApi.getInstance()
                    .listBuildings()
            })
            allBasesValue.clear()
            allBasesValue.addAll(withContext(Dispatchers.IO) {
                DbApi.getInstance()
                    .list(Base)
                    .map { it.toRaw() }
            })
        }
        allBases.emit(allBasesValue)
    }
}
