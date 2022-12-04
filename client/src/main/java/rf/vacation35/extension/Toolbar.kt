package rf.vacation35.extension

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import rf.vacation35.R
import rf.vacation35.screen.MainActivity
import splitties.activities.start

fun Toolbar.onBackPressed(handler: (View) -> Unit) {
    setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
    setNavigationOnClickListener(handler)
}

inline fun <reified T : Fragment> Toolbar.inflateNavMenu() {
    inflateMenu(R.menu.menu_nav)
    setOnMenuItemClickListener {
        when (it.itemId) {
            R.id.action_reload -> {
                (context.getActivity() as AppCompatActivity?)
                    ?.supportFragmentManager
                    ?.replaceFragment(android.R.id.content, T::class.java.newInstance(), false)
            }
            R.id.action_close -> {
                context.start<MainActivity> {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
        }
        true
    }
}
