package rf.vacation35.extension

import android.widget.EditText

val EditText.value get() = text.toString().trim()
