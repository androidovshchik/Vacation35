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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.databinding.FragmentFilterBinding
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.Base
import rf.vacation35.remote.dao.Building
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.intent?.getStringExtra(EXTRA_BASE_TITLE)?.let {
            binding.esBase.setText(it)
            binding.esBase.isEnabled = false
            binding.esBuilding.isEnabled = false
        }
        activity?.intent?.getStringExtra(EXTRA_BUILDING_TITLE)?.let {
            binding.esBuilding.setText(it)
            binding.esBase.isEnabled = false
            binding.esBuilding.isEnabled = false
        }
        binding.esBase.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    bases.value = emptyList()
                    replaceBuildings(emptyList())
                }
                1 -> {
                    bases.value = allBases.value
                    replaceBuildings(allBuildings.map { it.name })
                }
                else -> {
                    val base = allBases.value.getOrNull(max(0, position - 2))
                    if (base != null) {
                        bases.value = listOf(base)
                        val buildings = allBuildings.filter { it.base.id == base.id.value }
                        replaceBuildings(buildings.map { it.name })
                    }
                }
            }
        }
        binding.esBuilding.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> buildings.value = emptyList()
                1 -> {
                    val baseIds = bases.value.map { it.id.value }
                    buildings.value = allBuildings.filter { it.base.id in baseIds }
                }
                else -> {
                    val building = allBuildings.getOrNull(max(0, position - 2))
                    if (building != null) {
                        buildings.value = listOf(building)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            allBases.collect { items ->
                binding.esBase.updateList(items.map { it.name })
            }
        }
    }

    private fun replaceBuildings(items: List<String>) {
        buildings.value = emptyList()
        binding.esBuilding.updateList(items)
        binding.esBuilding.setText("")
    }

    suspend fun loadBuildings() {
        withContext(Dispatchers.IO) {
            while (true) {
                try {
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
