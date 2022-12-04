package rf.vacation35.screen

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import splitties.activities.start
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context,
    api: DbApi,
    preferences: Preferences
) : ViewModel() {

    init {
        viewModelScope.launch {
            while (true) {
                try {
                    val oldUser = preferences.user!!
                    val newUser = withContext(Dispatchers.IO) {
                        api.findUser(oldUser.login, oldUser.password)
                            ?.toRaw()
                    }
                    when {
                        newUser == null -> {
                            preferences.user = null
                            context.start<LoginActivity> {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            break
                        }
                        newUser != oldUser -> {
                            preferences.user = newUser
                            AbstractFragment.user.value = newUser
                        }
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
