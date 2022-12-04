package rf.vacation35.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.bbErrorDelay
import rf.vacation35.bbRepeatDelay
import rf.vacation35.checkUserDelay
import rf.vacation35.local.Preferences
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.Base
import rf.vacation35.remote.dao.User
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val api: DbApi,
    private val preferences: Preferences
) : ViewModel() {

    fun init() {
        viewModelScope.launch {
            while (true) {
                try {
                    val oldUser = preferences.user!!
                    val newUser = withContext(Dispatchers.IO) {
                        api.findUser(oldUser.login, oldUser.password)
                            ?.toRaw()
                    }
                    if (newUser != null) {
                        if (newUser != oldUser) {
                            preferences.user = newUser
                        }
                        AbstractFragment.user.value = newUser
                    } else {
                        preferences.user = null
                        AbstractFragment.user.value = User.None()
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
                    BBFragment.allBases.value = withContext(Dispatchers.IO) {
                        api.list(Base)
                            .map { it.toRaw() }
                    }
                    BBFragment.allBuildings.value = withContext(Dispatchers.IO) {
                        api.listBuildings()
                    }
                    delay(bbRepeatDelay)
                } catch (e: Throwable) {
                    Timber.e(e)
                    delay(bbErrorDelay)
                }
            }
        }
    }
}
