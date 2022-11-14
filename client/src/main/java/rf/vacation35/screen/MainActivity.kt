package rf.vacation35.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.android.calendar.month.MonthByWeekFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.R
import rf.vacation35.databinding.ActivityMainBinding
import rf.vacation35.databinding.DrawerHeaderBinding
import rf.vacation35.extension.addFragment
import rf.vacation35.extension.areYouSure
import rf.vacation35.local.Preferences
import rf.vacation35.remote.DbApi
import splitties.activities.start
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AbstractActivity() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var preferences: Preferences

    private lateinit var binding: ActivityMainBinding

    private lateinit var header: DrawerHeaderBinding

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

        updateHeader()
        lifecycleScope.launch {
            preferences.asFlow()
                .collect {
                    if (it == "user") {
                        updateHeader()
                    }
                }
        }
        lifecycleScope.launch {
            while (!isFinishing) {
                try {
                    val rawUser = preferences.rawUser!!
                    val user = withContext(Dispatchers.IO) {
                        api.findUser(rawUser.login, rawUser.password)
                    }
                    if (user != null) {
                        preferences.rawUser = user.raw
                    } else if (!isFinishing) {
                        start<LoginActivity> {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    }
                } catch (e: Throwable) {
                    Timber.e(e)
                } finally {
                    delay(60_000L)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateHeader() {
        val rawUser = preferences.rawUser!!
        header.login.text = "@${rawUser.login}"
        header.name.text = rawUser.name
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
}
