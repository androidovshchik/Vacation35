package rf.vacation35.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import rf.vacation35.local.Preferences
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(preferences: Preferences) : ViewModel() {

    val user = MutableStateFlow(preferences.user!!)

    init {
        viewModelScope.launch {
            preferences.asFlow()
                .filter { it == "user" }
                .collect {
                    user.value = preferences.user!!
                }
        }
    }
}
