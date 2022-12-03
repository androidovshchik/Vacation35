package rf.vacation35.screen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import rf.vacation35.*
import java.time.LocalDate
import java.util.concurrent.TimeUnit

private val startDelay = TimeUnit.SECONDS.toMillis(60)

abstract class AbstractActivity : AppCompatActivity() {

}

abstract class AbstractFragment : Fragment() {

    private var startJob: Job? = null

    private var startTime = 0L

    protected var argDate: LocalDate? = null

    protected val argUserId get() = activity?.intent?.getIntExtra(EXTRA_USER_ID, 0) ?: 0

    protected val argBaseId get() = activity?.intent?.getIntExtra(EXTRA_BASE_ID, 0) ?: 0

    protected val argBuildingId get() = activity?.intent?.getIntExtra(EXTRA_BUILDING_ID, 0) ?: 0

    protected val argBookingId get() = activity?.intent?.getLongExtra(EXTRA_BOOKING_ID, 0L) ?: 0L

    protected val argBids get() = activity?.intent?.let {
        if (it.hasExtra(EXTRA_BIDS)) it.getBooleanExtra(EXTRA_BIDS, false) else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        argDate = activity?.intent?.getSerializableExtra(EXTRA_DATE) as LocalDate?
    }

    protected fun launchStartJobIfNeeded(block: suspend CoroutineScope.() -> Unit) {
        val now = System.currentTimeMillis()
        if (now - startTime > startDelay) {
            startTime = now
            startJob?.cancel()
            startJob = lifecycleScope.launch(block = block)
        }
    }
}
