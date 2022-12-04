package rf.vacation35.screen

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import rf.vacation35.*
import rf.vacation35.extension.removeFragment
import rf.vacation35.extension.use
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.User
import splitties.snackbar.action
import splitties.snackbar.longSnack
import java.time.LocalDate
import javax.inject.Inject
import kotlin.reflect.KFunction0

abstract class AbstractActivity : AppCompatActivity() {

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

@AndroidEntryPoint
abstract class AbstractFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    protected val progress = ProgressDialog()

    protected var startJob: Job? = null

    private var startTime = 0L

    protected var argDate: LocalDate? = null
        private set

    protected val argUserId get() = activity?.intent?.getIntExtra(EXTRA_USER_ID, 0)
        ?: arguments?.getInt(EXTRA_USER_ID, 0)
        ?: 0

    protected val argBaseId get() = activity?.intent?.getIntExtra(EXTRA_BASE_ID, 0)
        ?: arguments?.getInt(EXTRA_BASE_ID, 0)
        ?: 0

    protected val argBuildingId get() = activity?.intent?.getIntExtra(EXTRA_BUILDING_ID, 0)
        ?: arguments?.getInt(EXTRA_BUILDING_ID, 0)
        ?: 0

    protected val argBookingId get() = activity?.intent?.getLongExtra(EXTRA_BOOKING_ID, 0L)
        ?: arguments?.getLong(EXTRA_BOOKING_ID, 0L)
        ?: 0L

    protected val argBids get() = when {
        activity?.intent?.hasExtra(EXTRA_BIDS) == true -> activity?.intent?.getBooleanExtra(EXTRA_BIDS, false)
        arguments?.containsKey(EXTRA_BIDS) == true -> arguments?.getBoolean(EXTRA_BIDS, false)
        else -> null
    }

    protected val mThis get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        argDate = (activity?.intent?.getSerializableExtra(EXTRA_DATE)
            ?: arguments?.getSerializable(EXTRA_BUILDING_ID)) as LocalDate?
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            user.collect {
                onUserChanged(it)
            }
        }
    }

    protected open fun onUserChanged(user: User.Raw) {
    }

    override fun onStart() {
        super.onStart()
        if (System.currentTimeMillis() - startTime > onStartDelay) {
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
            view?.longSnack("Ошибка при запросе") {
                action("Повторить", retry)
            }
        })
    }

    override fun onDestroyView() {
        childFragmentManager.removeFragment(progress)
        super.onDestroyView()
    }

    companion object {

        val user = MutableSharedFlow<User.Raw>(1)
    }
}
