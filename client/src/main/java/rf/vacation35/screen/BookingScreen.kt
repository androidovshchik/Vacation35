package rf.vacation35.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import rf.vacation35.databinding.FragmentBookingBinding
import rf.vacation35.databinding.FragmentListBinding
import rf.vacation35.extension.addFragment
import rf.vacation35.extension.onBackPressed
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

    private lateinit var binding: FragmentListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }
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

    private lateinit var binding: FragmentBookingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = activity?.intent?.getIntExtra("id", 0) ?: 0
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = if (id == 0) "Новая бронь" else "Бронь"
        }
    }
}
