package rf.vacation35.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.autsoft.krate.SimpleKrate
import hu.autsoft.krate.kotlinx.kotlinxPref
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class Preferences @Inject constructor(
    @ApplicationContext context: Context
) : SimpleKrate(context) {

    var userData: UserData? by kotlinxPref()

    fun asFlow() = callbackFlow<String> {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, propertyKey ->
            trySend(propertyKey)
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}
