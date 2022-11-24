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
import rf.vacation35.EXTRA_BASE_TITLE
import rf.vacation35.EXTRA_BUILDING_TITLE
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

    val buildings = MutableStateFlow(listOf<Building.Raw>())

    private val _bases = MutableStateFlow(listOf<Base>())

    private val _buildings = mutableListOf<Building.Raw>()

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
            buildings.tryEmit(emptyList())
            when (position) {
                0 -> binding.esBuilding.updateList(emptyList())
                1 -> binding.esBuilding.updateList(_buildings.map { it.name })
                else -> {
                    val base = _bases.value.getOrNull(max(0, position - 2))
                    if (base != null) {
                        val buildings = _buildings.filter { it.base.id == base.id.value }
                        binding.esBuilding.updateList(buildings.map { it.name })
                    }
                }
            }
            binding.esBuilding.setText("")
        }
        binding.esBuilding.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> buildings.tryEmit(emptyList())
                1 -> buildings.tryEmit(_buildings)
                else -> {
                    val building = _buildings.getOrNull(max(0, position - 2))
                    if (building != null) {
                        buildings.tryEmit(listOf(building))
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            _bases.collect { items ->
                binding.esBase.updateList(items.map { it.name })
            }
        }
    }

    suspend fun loadBuildings() {
        _buildings.clear()
        _bases.value = emptyList()
        withContext(Dispatchers.IO) {
            while (true) {
                try {
                    _buildings.addAll(withContext(Dispatchers.IO) {
                        DbApi.getInstance()
                            .listBuildings()
                            .sortedBy { it.name }
                    })
                    _bases.value = withContext(Dispatchers.IO) {
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

    fun selectAll() {
        if (_bases.value.isNotEmpty()) {
            binding.esBase.updateList(_bases.value.map { it.name })
            binding.esBase.setText("Все")
        }
        if (_buildings.isNotEmpty()) {
            buildings.value = _buildings
            binding.esBuilding.updateList(_buildings.map { it.name })
            binding.esBuilding.setText("Все")
        }
    }
}
