package rf.vacation35.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.android.calendar.month.MonthByWeekFragment
import rf.vacation35.databinding.ActivityBookingBinding
import ws.xsoh.etar.databinding.FullMonthByWeekBinding

class BookingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

class BookingFragment : MonthByWeekFragment(System.currentTimeMillis(), false) {

    private lateinit var binding: FullMonthByWeekBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        binding = FullMonthByWeekBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
}
