package rf.vacation35.extension

import android.content.Context
import android.content.DialogInterface
import android.util.TypedValue
import androidx.annotation.AttrRes
import splitties.alertdialog.alertDialog
import splitties.alertdialog.cancelButton
import splitties.alertdialog.okButton
import splitties.alertdialog.title
import splitties.dimensions.dp

fun Context.dp(value: Int) = dp(value).toFloat()

fun Context.dp(value: Float) = dp(value)

fun Context.sp(value: Int): Float = value * resources.displayMetrics.scaledDensity

fun Context.sp(value: Float): Float = value * resources.displayMetrics.scaledDensity

fun Context.getIdRes(@AttrRes id: Int): Int {
    val value = TypedValue()
    theme.resolveAttribute(id, value, true)
    return value.resourceId
}

fun Context.areYouSure(yes: (dialog: DialogInterface) -> Unit) {
    alertDialog {
        title = "Вы уверены?"
        okButton(yes)
        cancelButton()
    }.show()
}
