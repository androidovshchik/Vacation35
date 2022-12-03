package rf.vacation35.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.bbErrorDelay
import rf.vacation35.bbRepeatDelay
import rf.vacation35.local.Preferences
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.Base
import rf.vacation35.remote.dao.Building
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(preferences: Preferences) : ViewModel() {

    val user = MutableStateFlow(preferences.user!!)

    val bases = MutableSharedFlow<List<Base.Raw>>()

    val allBases = CopyOnWriteArrayList<Base.Raw>()

    val allBuildings = CopyOnWriteArrayList<Building.Raw>()

    init {
        viewModelScope.launch {
            preferences.asFlow()
                .filter { it == "user" }
                .collect {
                    user.value = preferences.user!!
                }
        }
        viewModelScope.launch {
            while (true) {
                try {
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
                    bases.emit(allBases)
                    delay(bbRepeatDelay)
                } catch (e: Throwable) {
                    Timber.e(e)
                    delay(bbErrorDelay)
                }
            }
        }
    }
}
