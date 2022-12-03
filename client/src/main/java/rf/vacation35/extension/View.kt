package rf.vacation35.extension

import android.view.View
import com.google.android.material.snackbar.Snackbar
import splitties.snackbar.snack

fun View.dp(value: Int) = context.dp(value)

fun View.dp(value: Float) = context.dp(value)

fun View.sp(value: Int) = context.sp(value)

fun View.sp(value: Float) = context.sp(value)

inline fun View.snack(
    e: Throwable?,
    duration: Int = Snackbar.LENGTH_SHORT,
    actionSetup: Snackbar.() -> Unit = {}
) {
    snack(e?.message ?: e.toString(), duration, actionSetup)
}
