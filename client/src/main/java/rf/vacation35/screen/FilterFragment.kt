package rf.vacation35.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
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

    val buildings = MutableSharedFlow<List<Building.Raw>>(0, 1, BufferOverflow.DROP_OLDEST)

    private lateinit var binding: FragmentFilterBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.esBase.setOnItemClickListener { _, _, position, _ ->
            buildings.tryEmit(emptyList())
            when (position) {
                0 -> binding.esBuilding.updateList(emptyList())
                1 -> binding.esBuilding.updateList(mBuildings.value.map { it.name })
                else -> {
                    val base = mBases.value.getOrNull(max(0, position - 2))
                    if (base != null) {
                        val buildings = mBuildings.value.filter { it.base.id == base.id.value }
                        binding.esBuilding.updateList(buildings.map { it.name })
                    }
                }
            }
            binding.esBuilding.setText("")
        }
        binding.esBuilding.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> buildings.tryEmit(emptyList())
                1 -> buildings.tryEmit(mBuildings.value)
                else -> {
                    val building = mBuildings.value.getOrNull(max(0, position - 2))
                    if (building != null) {
                        buildings.tryEmit(listOf(building))
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            mBases.collect { items ->
                binding.esBase.updateList(items.map { it.name })
            }
        }
    }

    suspend fun loadBuildings() {
        mBuildings.value = emptyList()
        mBases.value = emptyList()
        withContext(Dispatchers.IO) {
            while (true) {
                try {
                    mBuildings.value = withContext(Dispatchers.IO) {
                        DbApi.getInstance()
                            .listBuildings()
                            .sortedBy { it.name }
                    }
                    mBases.value = withContext(Dispatchers.IO) {
                        DbApi.getInstance()
                            .list(Base)
                            .sortedBy { it.name }
                    }
                    break
                } catch (e: Throwable) {
                    Timber.e(e)
                } finally {
                    delay(10_000L)
                }
            }
        }
    }

    companion object {

        private val mBases = MutableStateFlow(listOf<Base>())

        private val mBuildings = MutableStateFlow(listOf<Building.Raw>())
    }
}
