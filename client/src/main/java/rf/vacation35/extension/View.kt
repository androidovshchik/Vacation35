package rf.vacation35.extension

import android.view.View
import com.google.android.material.snackbar.Snackbar
import splitties.snackbar.snack

inline fun View.snack(
    e: Throwable?,
    duration: Int = Snackbar.LENGTH_SHORT,
    actionSetup: Snackbar.() -> Unit = {}
) {
    snack(e?.message ?: e.toString(), duration, actionSetup)
}
