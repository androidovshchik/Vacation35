package rf.vacation35.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.BuildConfig
import rf.vacation35.EXTRA_BIDS
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
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var preferences: Preferences

    private val calendar = CalendarFragment()

    private lateinit var binding: ActivityMainBinding

    private lateinit var header: DrawerHeaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (preferences.user == null) {
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
                R.id.action_bids -> {
                    start<BookingListActivity> {
                        putExtra(EXTRA_BIDS, true)
                    }
                }
                R.id.action_bookings -> {
                    start<BookingListActivity> {
                        putExtra(EXTRA_BIDS, false)
                    }
                }
                R.id.action_bases -> {
                    start<BaseListActivity>()
                }
                R.id.action_buildings -> {
                    start<BuildingListActivity>()
                }
                R.id.action_users -> {
                    start<AccountListActivity>()
                }
                R.id.action_logout -> {
                    areYouSure {
                        start<LoginActivity>()
                        finish()
                    }
                }
            }
            binding.drawer.closeDrawer(GravityCompat.START)
            true
        }

        if (savedInstanceState == null) {
            supportFragmentManager.addFragment(R.id.fl_container, calendar, false)
        }

        updateAccess()
        lifecycleScope.launch {
            preferences.asFlow()
                .collect {
                    if (it == "user") {
                        updateAccess()
                    }
                }
        }
        lifecycleScope.launch {
            while (!isFinishing) {
                try {
                    val oldUser = preferences.user!!
                    val newUser = withContext(Dispatchers.IO) {
                        api.findUser(oldUser.login, oldUser.password)
                    }
                    if (newUser != null) {
                        preferences.user = newUser.toRaw()
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
    private fun updateAccess() {
        val user = preferences.user!!
        with(header) {
            login.text = "@${user.login}"
            name.text = user.name
        }
        with(binding.navView.menu) {
            findItem(R.id.action_users).isVisible = user.admin || BuildConfig.DEBUG
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_today -> {
                calendar.scrollToToday()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
