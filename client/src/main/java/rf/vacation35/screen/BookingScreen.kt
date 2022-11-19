package rf.vacation35.screen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import rf.vacation35.extension.addFragment
import rf.vacation35.remote.DbApi
import javax.inject.Inject

@AndroidEntryPoint
class BookingListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, BookingListFragment(), false)
    }
}

@AndroidEntryPoint
class BookingListFragment : Fragment() {

    @Inject
    lateinit var api: DbApi
}

@AndroidEntryPoint
class BookingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, BookingFragment(), false)
    }
}

@AndroidEntryPoint
class BookingFragment : Fragment() {

    @Inject
    lateinit var api: DbApi
}
