package rf.vacation35.screen

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.bbErrorDelay
import rf.vacation35.bbRepeatDelay
import rf.vacation35.checkUserDelay
import rf.vacation35.local.Preferences
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.Base
import rf.vacation35.remote.dao.Building
import splitties.activities.start
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context,
    api: DbApi,
    preferences: Preferences
) : ViewModel() {

    val user = MutableStateFlow(preferences.user!!)

    val bases = MutableSharedFlow<List<Base.Raw>>()

    val allBases = CopyOnWriteArrayList<Base.Raw>()

    val allBuildings = CopyOnWriteArrayList<Building.Raw>()

    init {
        viewModelScope.launch {
            while (true) {
                try {
                    val oldUser = preferences.user!!
                    val newUser = withContext(Dispatchers.IO) {
                        api.findUser(oldUser.login, oldUser.password)
                            ?.toRaw()
                    }
                    if (newUser != null) {
                        preferences.user = newUser
                        user.value = newUser
                    } else {
                        preferences.user = null
                        context.start<LoginActivity> {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        break
                    }
                } catch (e: Throwable) {
                    Timber.e(e)
                } finally {
                    delay(checkUserDelay)
                }
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
