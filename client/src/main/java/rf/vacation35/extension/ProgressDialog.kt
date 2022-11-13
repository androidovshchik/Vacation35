package rf.vacation35.extension

import android.app.ProgressDialog

inline fun ProgressDialog.with(action: () -> Unit, catch: (Throwable) -> Unit) {
    show()
    try {
        action()
    } catch (e: Throwable) {
        catch(e)
    } finally {
        dismiss()
    }
}
