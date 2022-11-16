package rf.vacation35.screen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import rf.vacation35.databinding.FragmentCalendarBinding
import rf.vacation35.databinding.ItemMonthBinding
import rf.vacation35.extension.*
import rf.vacation35.remote.DbApi
import javax.inject.Inject

@AndroidEntryPoint
class CalendarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, CalendarFragment(), false)
    }
}

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    private val progress = ProgressDialog()

    private lateinit var binding: FragmentCalendarBinding

    private var listJob: Job? = null

    @SuppressLint("SetTextI18n")
    private val adapter = monthAdapter {
        onCreateViewHolder { parent ->
            with(ItemMonthBinding.inflate(layoutInflater, parent, false)) {
                AbstractAdapter.ViewHolder(this).apply {
                    root.setOnClickListener {
                        try {
                            val item = items[bindingAdapterPosition]

                        } catch (ignored: Throwable) {
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvCalendar.adapter = adapter
        binding.fabAdd.setOnClickListener {

        }
    }

    override fun onDestroyView() {
        childFragmentManager.removeFragment(progress)
        super.onDestroyView()
    }
}
