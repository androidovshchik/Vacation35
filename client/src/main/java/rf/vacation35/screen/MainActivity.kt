package rf.vacation35.screen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.android.calendar.month.MonthByWeekFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import rf.vacation35.R
import rf.vacation35.databinding.ActivityMainBinding
import rf.vacation35.databinding.DrawerHeaderBinding
import rf.vacation35.extension.addFragment
import rf.vacation35.extension.areYouSure
import rf.vacation35.local.Preferences
import splitties.activities.start
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AbstractActivity() {

    @Inject
    lateinit var preferences: Preferences

    private lateinit var binding: ActivityMainBinding

    private lateinit var header: DrawerHeaderBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (preferences.rawUser == null) {
            start<LoginActivity>()
            finish()
            return
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        header = DrawerHeaderBinding.bind(binding.navView.getHeaderView(0))
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(this, binding.drawer, binding.toolbar, 0, 0)
        binding.drawer.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_bookings -> {
                    binding.toolbar.title = "Брони"
                }
                R.id.action_bids -> {
                    binding.toolbar.title = "Заявки"
                }
                R.id.action_buildings -> {

                }
                R.id.action_bases -> {
                    start<BaseListActivity>()
                }
                R.id.action_users -> {
                    start<AccountListActivity>()
                }
                R.id.action_logout -> {
                    areYouSure {
                        preferences.rawUser = null
                        start<LoginActivity>()
                        finish()
                    }
                }
            }
            binding.drawer.closeDrawer(GravityCompat.START)
            true
        }

        fragmentManager.addFragment(R.id.fl_container, MonthByWeekFragment(System.currentTimeMillis(), false))

        lifecycleScope.launch {
            preferences.asFlow()
                .collect {
                    if (it == "user") {
                        val rawUser = preferences.rawUser!!
                        header.login.text = "@${rawUser.login}"
                        header.name.text = rawUser.name
                    }
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
}
