package rf.vacation35.extension

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CancellationException
import timber.log.Timber

val FragmentManager.topFragment: Fragment?
    get() = findFragmentByTag((backStackEntryCount - 1).toString())

fun FragmentManager.showFragment(id: Int) {
    findFragmentById(id)?.let {
        beginTransaction()
            .show(it)
            .commitAllowingStateLoss()
        executePendingTransactions()
    }
}

fun FragmentManager.hideFragment(id: Int) {
    findFragmentById(id)?.let {
        beginTransaction()
            .hide(it)
            .commitAllowingStateLoss()
        executePendingTransactions()
    }
}

inline fun FragmentManager.use(
    id: Int,
    fragment: Fragment,
    body: () -> Unit,
    catch: (Throwable) -> Unit,
    final: () -> Unit = { removeFragment(fragment) }
) {
    addFragment(id, fragment, false)
    try {
        body()
    } catch (_: CancellationException) {
    } catch (e: Throwable) {
        Timber.e(e)
        catch(e)
    } finally {
        final()
    }
}

fun FragmentManager.addFragment(id: Int, fragment: Fragment, backStack: Boolean = true) {
    try {
        beginTransaction()
            .add(id, fragment, backStackEntryCount.toString())
            .apply {
                if (backStack) {
                    addToBackStack(fragment.javaClass.name)
                }
            }
            .commitAllowingStateLoss()
        executePendingTransactions()
    } catch (ignored: Throwable) {
    }
}

fun FragmentManager.replaceFragment(id: Int, fragment: Fragment, backStack: Boolean = true) {
    beginTransaction()
        .replace(id, fragment, backStackEntryCount.toString())
        .apply {
            if (backStack) {
                addToBackStack(fragment.javaClass.name)
            }
        }
        .commitAllowingStateLoss()
    executePendingTransactions()
}

fun FragmentManager.removeFragment(fragment: Fragment) {
    try {
        beginTransaction()
            .remove(fragment)
            .commitAllowingStateLoss()
        executePendingTransactions()
    } catch (ignored: Throwable) {
    }
}

fun FragmentManager.popFragment(name: String?, immediate: Boolean): Boolean {
    return if (name != null) {
        if (immediate) {
            popBackStackImmediate(name, 0)
        } else {
            popBackStack(name, 0)
            true
        }
    } else {
        if (immediate) {
            popBackStackImmediate()
        } else {
            popBackStack()
            true
        }
    }
}
