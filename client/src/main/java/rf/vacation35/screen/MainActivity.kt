package rf.vacation35.screen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import rf.vacation35.BuildConfig
import rf.vacation35.EXTRA_BIDS
import rf.vacation35.R
import rf.vacation35.databinding.ActivityMainBinding
import rf.vacation35.databinding.DrawerHeaderBinding
import rf.vacation35.extension.addFragment
import rf.vacation35.extension.areYouSure
import rf.vacation35.local.Preferences
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.User
import splitties.activities.start
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AbstractActivity() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var preferences: Preferences

    private val viewModel: MainViewModel by viewModels()

    private val calendar = CalendarFragment()

    private lateinit var binding: ActivityMainBinding

    private lateinit var header: DrawerHeaderBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        logout()
                    }
                }
                R.id.action_settings -> {

                }
            }
            binding.drawer.closeDrawer(GravityCompat.START)
            true
        }

        if (savedInstanceState == null) {
            supportFragmentManager.addFragment(R.id.fl_container, calendar, false)
        }

        lifecycleScope.launch {
            AbstractFragment.user.collect {
                if (it !is User.None) {
                    with(header) {
                        login.text = "@${it.login}"
                        name.text = it.name
                    }
                    with(binding.navView.menu) {
                        findItem(R.id.action_users).isVisible = it.admin || BuildConfig.DEBUG
                    }
                } else {
                    logout()
                }
            }
        }
        viewModel.checkupUser()
    }

    private fun logout() {
        preferences.user = null
        start<LoginActivity>()
        finish()
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
            R.id.action_bids -> {
                start<BookingListActivity> {
                    putExtra(EXTRA_BIDS, true)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
