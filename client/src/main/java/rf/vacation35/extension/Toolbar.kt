package rf.vacation35.extension

import android.view.View
import androidx.appcompat.widget.Toolbar
import rf.vacation35.R

fun Toolbar.onBackPressed(handler: (View) -> Unit) {
    setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
    setNavigationOnClickListener(handler)
}
