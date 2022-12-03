package rf.vacation35.screen

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import rf.vacation35.*
import rf.vacation35.extension.removeFragment
import rf.vacation35.extension.use
import rf.vacation35.local.Preferences
import rf.vacation35.remote.DbApi
import splitties.snackbar.action
import splitties.snackbar.snackForever
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.reflect.KFunction0

private val startDelay = TimeUnit.SECONDS.toMillis(60)

abstract class AbstractActivity : AppCompatActivity() {

}

@AndroidEntryPoint
abstract class AbstractFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var preferences: Preferences

    protected val progress = ProgressDialog()

    protected var startJob: Job? = null

    protected var startTime = 0L

    protected var argDate: LocalDate? = null
        private set

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

    override fun onStart() {
        super.onStart()
        if (System.currentTimeMillis() - startTime > startDelay) {
            readOnStart()
        }
    }

    @CallSuper
    protected open fun readOnStart() {
        startTime = System.currentTimeMillis()
    }

    protected open fun upsert() {
    }

    protected open fun delete() {
    }

    protected inline fun useProgress(
        retry: KFunction0<Unit>,
        fragment: Fragment = progress,
        body: () -> Unit
    ) {
        childFragmentManager.use(R.id.fl_fullscreen, fragment, body, {
            view?.snackForever("Ошибка при запросе") {
                action("Повторить", retry)
                action("Отмена") {
                    dismiss()
                }
            }
        })
    }

    override fun onDestroyView() {
        childFragmentManager.removeFragment(progress)
        super.onDestroyView()
    }
}
