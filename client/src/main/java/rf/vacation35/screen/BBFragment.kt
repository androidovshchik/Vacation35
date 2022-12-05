package rf.vacation35.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import rf.vacation35.databinding.FragmentBbhBinding
import rf.vacation35.databinding.FragmentBbvBinding
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.Base
import rf.vacation35.remote.dao.Building
import rf.vacation35.screen.view.EditSpinner
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max

@AndroidEntryPoint
class BBVFragment : BBFragment() {

    override val baseSpinner get() = binding.esBase

    override val buildingSpinner get() = binding.esBuilding

    private lateinit var binding: FragmentBbvBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBbvBinding.inflate(inflater, container, false)
        return binding.root
    }
}

@AndroidEntryPoint
class BBHFragment : BBFragment() {

    override val baseSpinner get() = binding.esBase

    override val buildingSpinner get() = binding.esBuilding

    private lateinit var binding: FragmentBbhBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBbhBinding.inflate(inflater, container, false)
        return binding.root
    }
}

@AndroidEntryPoint
abstract class BBFragment : AbstractFragment() {

    protected abstract val baseSpinner: EditSpinner

    protected abstract val buildingSpinner: EditSpinner

    val bases = MutableLiveData<List<Base.Raw>>()

    val buildings = MutableLiveData<List<Building.Raw>>()

    private var baseId: Int? = null

    private var buildingId: Int? = null

    private val filteredBuildings: List<Building.Raw> get() {
        val baseIds = bases.value?.map { it.id }.orEmpty()
        return allBuildings.filter { it.base!!.id in baseIds }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        baseSpinner.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    baseId = null
                    bases.value = emptyList()
                    selectBuildings(emptyList())
                }
                1 -> {
                    baseId = null
                    bases.value = allBases
                    selectBuildings(allBuildings)
                }
                else -> {
                    val base = allBases.getOrNull(max(0, position - 2))
                    baseId = base?.id
                    if (base != null) {
                        bases.value = listOf(base)
                        selectBuildings(filteredBuildings)
                    }
                }
            }
        }
        buildingSpinner.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    buildingId = null
                    buildings.value = emptyList()
                }
                1 -> {
                    buildingId = null
                    buildings.value = filteredBuildings
                }
                else -> {
                    val building = filteredBuildings.getOrNull(max(0, position - 2))
                    buildingId = building?.id
                    if (building != null) {
                        buildings.value = listOf(building)
                    }
                }
            }
        }
        allBoth.observe(viewLifecycleOwner) {
            selectBoth(baseId ?: argBaseId, buildingId ?: argBuildingId)
        }
    }

    fun selectBoth(baseId: Int = 0, buildingId: Int = 0) {
        @Suppress("NAME_SHADOWING")
        var baseId = baseId
        var base = allBases.firstOrNull { it.id == baseId }
        val building = allBuildings.firstOrNull { it.id == buildingId }
        if (base == null && building != null) {
            baseId = building.base!!.id
            base = allBases.firstOrNull { it.id == baseId }
        }
        if (base != null) {
            selectBases(listOf(base))
        } else {
            selectBases(allBases)
        }
        when {
            building?.base?.id == baseId -> selectBuildings(listOf(building))
            base != null -> selectBuildings(filteredBuildings)
            else -> selectBuildings(allBuildings)
        }
    }

    private fun selectBases(value: List<Base.Raw>) {
        bases.value = value
        baseSpinner.updatePopup(allBases)
        baseSpinner.setText(when(value.size) {
            0 -> ""
            1 -> value.first().name
            else -> "Все"
        })
        baseSpinner.clearFocus()
    }

    private fun selectBuildings(value: List<Building.Raw>) {
        buildings.value = value
        buildingSpinner.updatePopup(filteredBuildings)
        buildingSpinner.setText(when(value.size) {
            0 -> ""
            1 -> value.first().name
            else -> "Все"
        })
        buildingSpinner.clearFocus()
    }

    suspend fun loadBaseBuildings() {
        withContext(Dispatchers.IO) {
            allBuildings.clear()
            allBuildings.addAll(withContext(Dispatchers.IO) {
                DbApi.getInstance()
                    .listBuildings()
            })
            allBases.clear()
            allBases.addAll(withContext(Dispatchers.IO) {
                DbApi.getInstance()
                    .list(Base)
                    .map { it.toRaw() }
            })
        }
        allBoth.value = Unit
    }

    companion object {

        val allBoth = MutableLiveData<Unit>()

        val allBases = CopyOnWriteArrayList<Base.Raw>()

        val allBuildings = CopyOnWriteArrayList<Building.Raw>()
    }
}
