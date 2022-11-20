package rf.vacation35.extension

import android.content.Intent
import android.view.View
import androidx.appcompat.widget.Toolbar
import rf.vacation35.R
import rf.vacation35.screen.MainActivity
import splitties.activities.start

fun Toolbar.onBackPressed(handler: (View) -> Unit) {
    setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
    setNavigationOnClickListener(handler)
}

fun Toolbar.inflateNavMenu() {
    inflateMenu(R.menu.menu_nav)
    setOnMenuItemClickListener {
        when (it.itemId) {
            R.id.action_close -> {
                context.start<MainActivity> {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
        }
        true
    }
}
