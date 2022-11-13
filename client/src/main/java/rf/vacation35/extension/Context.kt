package rf.vacation35.extension

import android.content.Context
import android.content.DialogInterface
import splitties.alertdialog.alertDialog
import splitties.alertdialog.cancelButton
import splitties.alertdialog.okButton
import splitties.alertdialog.title

fun Context.areYouSure(yes: (dialog: DialogInterface) -> Unit) {
    alertDialog {
        title = "Вы уверены?"
        okButton(yes)
        cancelButton()
    }.show()
}
