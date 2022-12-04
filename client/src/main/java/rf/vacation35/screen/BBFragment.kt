package rf.vacation35.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import rf.vacation35.databinding.FragmentBbhBinding
import rf.vacation35.databinding.FragmentBbvBinding
import rf.vacation35.remote.dao.Base
import rf.vacation35.remote.dao.Building
import rf.vacation35.screen.view.EditSpinner
import kotlin.math.max

@AndroidEntryPoint
class BBVFragment : BBFragment() {

    override val autofill = false

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

    override val autofill = true

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

    protected abstract val autofill: Boolean

    protected abstract val baseSpinner: EditSpinner

    protected abstract val buildingSpinner: EditSpinner

    val bases = MutableStateFlow(listOf<Base.Raw>())

    val buildings = MutableStateFlow(listOf<Building.Raw>())

    private val filteredBuildings: List<Building.Raw> get() {
        val baseIds = bases.value.map { it.id }
        return allBuildings.value.filter { it.base!!.id in baseIds }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        baseSpinner.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    bases.value = emptyList()
                    selectBuildings(emptyList())
                }
                1 -> {
                    bases.value = allBases.value
                    selectBuildings(allBuildings.value)
                }
                else -> {
                    val base = allBases.value.getOrNull(max(0, position - 2))
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
            allBuildings.collectIndexed { i, _ ->
                if (i == 0) {
                    if (autofill) {
                        selectAll(argBaseId, argBuildingId)
                    }
                } else {
                    val baseIds = allBases.value.map { it.id }
                    selectBases(bases.value.filter { it.id in baseIds })
                    selectBuildings(filteredBuildings)
                }
            }
        }
    }

    fun selectAll(baseId: Int = 0, buildingId: Int = 0) {
        @Suppress("NAME_SHADOWING")
        var baseId = baseId
        var base = allBases.value.firstOrNull { it.id == baseId }
        val building = allBuildings.value.firstOrNull { it.id == buildingId }
        if (base == null && building != null) {
            baseId = building.base!!.id
            base = allBases.value.firstOrNull { it.id == baseId }
        }
        if (base != null) {
            selectBases(listOf(base))
        } else {
            selectBases(allBases.value)
        }
        when {
            building?.base?.id == baseId -> selectBuildings(listOf(building))
            base != null -> selectBuildings(filteredBuildings)
            else -> selectBuildings(allBuildings.value)
        }
    }

    private fun selectBases(value: List<Base.Raw>) {
        bases.value = value
        baseSpinner.updatePopup(allBases.value)
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

    companion object {

        val allBases = MutableStateFlow<List<Base.Raw>>(emptyList())

        val allBuildings = MutableStateFlow<List<Building.Raw>>(emptyList())
    }
}
