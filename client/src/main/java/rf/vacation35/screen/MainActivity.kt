package rf.vacation35.screen

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import dagger.hilt.android.AndroidEntryPoint
import rf.vacation35.R
import rf.vacation35.databinding.ActivityMainBinding
import rf.vacation35.local.Preferences
import splitties.activities.start
import splitties.alertdialog.alertDialog
import splitties.alertdialog.cancelButton
import splitties.alertdialog.okButton
import splitties.alertdialog.title
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferences: Preferences

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (preferences.userData == null) {
            start<LoginActivity>()
            finish()
            return
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.iToolbar.toolbar)
        val toggle = ActionBarDrawerToggle(this, binding.drawer, binding.iToolbar.toolbar, 0, 0)
        binding.drawer.addDrawerListener(toggle)
        toggle.syncState()

        /*with(nv_main.getHeaderView(0)) {
            tv_name.text = preferences.name
            iv_settings.setOnClickListener {
                navController.navigateOnce(R.id.settings_fragment)
            }
        }*/
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_logout -> {
                    alertDialog {
                        title = "Вы уверены?"
                        okButton {
                            preferences.userData = null
                            start<LoginActivity>()
                            finish()
                        }
                        cancelButton()
                    }.show()
                }
            }
            binding.drawer.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
}
